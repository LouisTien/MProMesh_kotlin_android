package zyxel.com.multyproneo.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import zyxel.com.multyproneo.fragment.MeshTopologyPageFragment
import zyxel.com.multyproneo.util.LogUtil


class MeshTopologyPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val TAG = "MeshTopologyPageAdapter"

    override fun createFragment(position: Int): Fragment {
        LogUtil.d(TAG,"createFragment position:${position}")
        return fragments[position]
    }

    var fragments: ArrayList<Fragment> = arrayListOf(
        MeshTopologyPageFragment(),
        MeshTopologyPageFragment(),
        MeshTopologyPageFragment()
    )

    override fun getItemCount(): Int {
        LogUtil.d(TAG,"getItemCount:${fragments.size}")
        return fragments.size
    }

}
