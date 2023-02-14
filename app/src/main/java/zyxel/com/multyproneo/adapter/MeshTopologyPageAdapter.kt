package zyxel.com.multyproneo.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import zyxel.com.multyproneo.util.LogUtil


class MeshTopologyPageAdapter(
    private val fragmentList: MutableList<Fragment>,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private val TAG = "MeshTopologyPageAdapter"

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }


    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
        notifyDataSetChanged()
    }

    fun removeFragment(position: Int) {
        fragmentList.removeAt(position)
        notifyDataSetChanged()
    }
}
