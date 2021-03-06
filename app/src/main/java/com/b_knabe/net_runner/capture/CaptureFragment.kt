package com.b_knabe.net_runner.capture

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.b_knabe.packet_capture.core.nat.NatSession
import com.b_knabe.packet_capture.core.nat.NatSessionListHelper
import com.b_knabe.packet_capture.core.util.common.TimeFormatter
import com.b_knabe.packet_capture.core.util.net_utils.tcp.TcpDataSaver
import com.b_knabe.packet_capture.core.vpn.VpnEventHandler
import com.b_knabe.packet_capture.core.vpn.VpnServiceImpl
import com.b_knabe.net_runner.R
import com.b_knabe.net_runner.util.APP_ACTIVITY
import com.b_knabe.net_runner.util.hideViews
import com.b_knabe.net_runner.util.showViews
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.floating_action_button.*
import kotlinx.android.synthetic.main.floating_action_button.view.*
import kotlinx.android.synthetic.main.fragment_capture.*
import kotlinx.android.synthetic.main.fragment_capture.view.*
import java.util.*

class CaptureFragment : Fragment() {
    private val packets: MutableList<String> = LinkedList()
    private val sessionList: MutableList<NatSession> = ArrayList()
    private var sharedPrefsEdit: SharedPreferences.Editor? = null
    private var adapter: PacketAdapter? = null
    private var isNightMode = false
    private var buttonStateStart = true

    private fun clearCacheFinished() {
        packets.clear()
        sessionList.clear()
        adapter?.notifyDataSetChanged()
        showViews(placeholder_no_data, cloud_img_no_data)
        start_capture.playAnimation()
        Snackbar.make(container, getString(R.string.cache_cleared_tip),
                Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_capture, container, false)
        initView(rootView)
        return rootView
    }

    //To properly init view's and wait getView() call on each synthetic view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefsInit()
        APP_ACTIVITY.invalidateOptionsMenu() // now onCreateOptionsMenu(...) is called again
    }

    private fun initView(root: View) {
        adapter = PacketAdapter(packets, sessionList, requireContext())
        val recyclerView: RecyclerView = root.findViewById(R.id.rv_packet)
        root.start_capture.setMinAndMaxProgress(0.0f, 0.90f)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(),
                DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        if (packets.size == 0) {
            showViews(root.placeholder_no_data, root.cloud_img_no_data)
        }

        root.cardViewStartStop.setOnClickListener {
            if (buttonStateStart) {
                startCapture()
            } else {
                stopCapture()
            }
            buttonStateStart = !buttonStateStart
        }

        //open packet and get details about current request/response
        adapter?.setOnItemClickListener(OnItemClickListener { _, _, position, _ ->
            if (sessionList.size == 0) return@OnItemClickListener
            val session = sessionList[position]
            val dir = StringBuilder()
                    .append(TcpDataSaver.DATA_DIR)
                    .append(TimeFormatter.formatToYYMMDDHHMMSS(session.vpnStartTime))
                    .append("/")
                    .append(session.uniqueName)
                    .toString()
            val intent = Intent(requireActivity(), PacketDetail::class.java)
            intent.putExtra(KEY_DIR, dir)
            startActivity(intent)
        })
    }

    //override options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_capture, menu)

        if (Build.VERSION.SDK_INT >= Q) {
            val item = menu.findItem(R.id.dark_mode)
            item.isVisible = false

            val nightModeFlags = APP_ACTIVITY.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> cloud_img_no_data.setImageResource(R.drawable.ic_no_data_white)
                Configuration.UI_MODE_NIGHT_NO -> cloud_img_no_data.setImageResource(R.drawable.ic_no_data)
                /*Configuration.UI_MODE_NIGHT_UNDEFINED -> do()*/
            }
        }

        menu.getItem(1).setOnMenuItemClickListener {
            Observable
                    .create<Void> { emitter ->
                        NatSessionListHelper.clearCache()
                        emitter.onComplete()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete { clearCacheFinished() }
                    .subscribe()
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dark_mode -> darkMode()
        }
        return true
    }

    @SuppressLint("CommitPrefEdits")
    private fun sharedPrefsInit() {
        // force dark for Q and higher. Lower then Q would set dark theme with help of action bar button.
        if (Build.VERSION.SDK_INT < Q) {
            val appSettingPref: SharedPreferences = requireActivity().getSharedPreferences("AppSettingPrefs", 0)
            sharedPrefsEdit = appSettingPref.edit()
            isNightMode = appSettingPref.getBoolean("NightMode", false)

            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                cloud_img_no_data.setImageResource(R.drawable.ic_no_data_white)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                cloud_img_no_data.setImageResource(R.drawable.ic_no_data)
            }
        }
    }

    private fun darkMode() {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            cloud_img_no_data.setImageResource(R.drawable.ic_no_data)
            sharedPrefsEdit?.putBoolean("NightMode", false)
            sharedPrefsEdit?.apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            cloud_img_no_data.setImageResource(R.drawable.ic_no_data_white)
            sharedPrefsEdit?.putBoolean("NightMode", true)
            sharedPrefsEdit?.apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val intent = Intent(requireActivity(), VpnServiceImpl::class.java)
            intent.putExtra(KEY_CMD, 0)
            requireActivity().startService(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val event = VpnEventHandler.getInstance()
        event.setOnPacketListener {
            val sessions = NatSessionListHelper.getAllSessions()
            Handler(Looper.getMainLooper()).post {
                packets.clear()
                sessionList.clear()
                for (session in sessions) {
                    if (session.isHttp) {
                        packets.add(String.format(Locale.getDefault(),
                                "%s: %s", session.method, session.requestUrl))
                        sessionList.add(session)
                    }
                }
                if (packets.size > 0) {
                    hideViews(placeholder_no_data, cloud_img_no_data)
                } else {
                    showViews(placeholder_no_data, cloud_img_no_data)
                }
                adapter?.notifyDataSetChanged()
            }
        }

        event.setOnStartListener {
            hideViews(start_capture)
            //Make animation + change color
            cardViewStartStop.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.stop))
            showViews(stop_capture)
        }
        event.setOnStopListener {
            cardViewStartStop.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.start))
            showViews(start_capture)
            //Make animation
            hideViews(stop_capture)
        }
    }

    override fun onStop() {
        super.onStop()
        VpnEventHandler.getInstance().cancelAll()
    }

    private fun startCapture() {
        val intent = VpnService.prepare(requireContext())
        if (intent == null) {
            onActivityResult(0, Activity.RESULT_OK, null)
        } else {
            startActivityForResult(intent, 0)
        }
    }

    private fun stopCapture() {
        val intent = Intent(requireActivity(), VpnServiceImpl::class.java)
        intent.putExtra(KEY_CMD, 1)
        APP_ACTIVITY.startService(intent)
    }

    companion object {
        private const val KEY_CMD = "key_cmd"
        private const val KEY_DIR = "key_dir"
        fun newInstance(): CaptureFragment {
            return CaptureFragment()
        }
    }
}