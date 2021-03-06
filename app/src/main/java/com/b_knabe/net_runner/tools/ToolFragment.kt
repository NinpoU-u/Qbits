package com.b_knabe.net_runner.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.b_knabe.net_runner.R
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
        //set all listeners to tools cards
        rootView.card_url_coder.setOnClickListener {
            val fragment: Fragment = UrlCoderFragment()
            replaceFragment(fragment)
        }
        rootView.card_base64.setOnClickListener {
            val fragment: Fragment = Base64CoderFragment()
            replaceFragment(fragment)
        }
        rootView.card_md5.setOnClickListener {
            val fragment: Fragment = Md5Fragment()
            replaceFragment(fragment)
        }
        rootView.card_timestamp.setOnClickListener {
            val fragment: Fragment = TimestampFragment()
            replaceFragment(fragment)
        }
        rootView.card_ping.setOnClickListener {
            val fragment: Fragment = PingFragment()
            replaceFragment(fragment)
        }
        rootView.vpn.setOnClickListener {
            //VPN has low level logic and can't be fragmented
            val fragment: Fragment = VpnFragment()
            replaceFragment(fragment)
        }
    }
    //replace mechanism inside frame container
    private fun replaceFragment(someFragment: Fragment) {
        val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
        )
        transaction?.replace(R.id.frame_container, someFragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
    }
}