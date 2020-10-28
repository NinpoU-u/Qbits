package com.ninpou.qbits.tool

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ninpou.qbits.R
import kotlinx.android.synthetic.main.fragment_tools.view.*

class ToolFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_tools, container, false)
        setListenersToView(rootView)
        return rootView
    }

    private fun setListenersToView(rootView: View) {

        rootView.card_url_coder.setOnClickListener {
            val intent = Intent(activity, UrlCoderActivity::class.java)
            startActivity(intent)
        }
        rootView.card_base64.setOnClickListener {
            val intent = Intent(activity, Base64CoderActivity::class.java)
            startActivity(intent)
        }
        rootView.card_md5.setOnClickListener {
            val intent = Intent(activity, Md5Activity::class.java)
            startActivity(intent)
        }
        rootView.card_timestamp.setOnClickListener {
            val fragment: Fragment = TimestampFragment()
            replaceFragment(fragment)
        }
        rootView.card_ping.setOnClickListener {
            val intent = Intent(activity, PingActivity::class.java)
            startActivity(intent)
        }
        rootView.vpn.setOnClickListener {
            val intent = Intent(activity, VpnActivity::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(someFragment: Fragment) {
        val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.frame_container, someFragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
    }
}