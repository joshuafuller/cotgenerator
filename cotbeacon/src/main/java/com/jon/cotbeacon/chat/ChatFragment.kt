package com.jon.cotbeacon.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.cot.CotTeam
import com.jon.common.di.IUiResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.IStatusRepository
import com.jon.common.service.ServiceState
import com.jon.common.utils.Notify
import com.jon.cotbeacon.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var uiResources: IUiResources

    @Inject
    lateinit var chatRepository: IChatRepository

    @Inject
    lateinit var deviceUidRepository: IDeviceUidRepository

    @Inject
    lateinit var statusRepository: IStatusRepository

    private lateinit var adapter: ChatAdapter

    private lateinit var chatTextInput: TextInputLayout
    private lateinit var statusText: TextView
    private lateinit var disabledBox: ImageView

    private val serviceCommunicator by lazy { requireActivity() as IChatServiceCommunicator }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.fragment_chat, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatTextInput = view.findViewById(R.id.chat_message_input)
        statusText = view.findViewById(R.id.chat_status)
        disabledBox = view.findViewById(R.id.disabled_box)

        /* Colour the hint message with the accent */
        chatTextInput.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), uiResources.accentColourId))

        /* Bring the shaded box to the front, then have it intercept all touch events (when visible, of course) */
        disabledBox.bringToFront()
        disabledBox.setOnTouchListener { _, _ -> true }

        val context = requireContext()
        initialiseRecyclerView(view, context)
        initialiseSendButton(view, context)
        observeServiceStatus()
        observeChatStatus()
    }

    private fun initialiseRecyclerView(view: View, context: Context) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context).also {
            /* When new data is added, put it at the bottom of the view */
            it.stackFromEnd = true
        }
        recyclerView.adapter = ChatAdapter(context).also { adapter = it }
    }

    private fun initialiseSendButton(view: View, context: Context) {
        val sendButton = view.findViewById<Button>(R.id.chat_send_button)
        sendButton.setBackgroundColor(ContextCompat.getColor(context, uiResources.accentColourId))
        val icon = ContextCompat.getDrawable(context, R.drawable.send)
        sendButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        sendButton.setOnClickListener {
            getInputText()?.let { sendChat(it) }
        }
    }

    private fun sendChat(inputMessage: String) {
        val chat = ChatCursorOnTarget(isSelf = true).apply {
            uid = deviceUidRepository.getUid()
            messageUid = UUID.randomUUID().toString()
            team = CotTeam.fromPrefs(prefs)
            callsign = prefs.getStringFromPair(CommonPrefs.CALLSIGN)
            message = inputMessage
            outputPreset = OutputPreset.fromPrefs(prefs)
        }
        serviceCommunicator.sendChat(chat)
        chatTextInput.editText?.text?.clear()
    }

    private fun observeServiceStatus() {
        statusRepository.getStatus().observe(viewLifecycleOwner) {
            if (it == ServiceState.RUNNING) {
                statusText.visibility = View.GONE
                disabledBox.visibility = View.GONE
            } else {
                statusText.visibility = View.VISIBLE
                disabledBox.visibility = View.VISIBLE
            }
        }
    }

    private fun observeChatStatus() {
        chatRepository.getChats().observe(viewLifecycleOwner) {
            adapter.updateChats(it)
        }
        chatRepository.getErrors().observe(viewLifecycleOwner) {
            Notify.red(requireView(), "Chat error: $it")
        }
    }

    private fun getInputText(): String? {
        return chatTextInput.editText?.text?.toString()?.trim()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}