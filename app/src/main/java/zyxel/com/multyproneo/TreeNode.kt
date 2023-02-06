package zyxel.com.multyproneo

import zyxel.com.multyproneo.model.DevicesInfoObject


/**
 * Provides an easy way to create a parent-->child tree while preserving their depth/history.
 * Original Author: Jonathan, https://stackoverflow.com/a/22419453/14720622
 */
class TreeNode<T> {
    private val children: MutableList<TreeNode<T>>
    var parent: TreeNode<T>?
        private set
    var data: T
    var depth: Int

    constructor(data: T) {
        // a fresh node, without a parent reference
        children = ArrayList()
        parent = null
        this.data = data
        depth = 0 // 0 is the base level (only the root should be on there)
    }

    constructor(data: T, parent: TreeNode<T>) {
        // new node with a given parent
        children = ArrayList()
        this.data = data
        this.parent = parent
        depth = parent.depth + 1
        parent.addChild(this)
    }

    fun getChildren(): List<TreeNode<T>> {
        return children
    }

    fun setParent(parent: TreeNode<T>) {
        depth = parent.depth + 1
        parent.addChild(this)
        this.parent = parent
    }

    fun addChild(data: T) {
        val child = TreeNode(data)
        children.add(child)
    }

    fun addChild(child: TreeNode<T>) {
        children.add(child)
    }

    val isRootNode: Boolean
        get() = parent == null

    val isLeafNode: Boolean
        get() = children.size == 0

    fun removeParent() {
        parent = null
    }

    override fun toString(): String {
        var out = ""
            out += "Node: " + data.toString() + " | Depth: " + depth + " | Parent: " + (if (parent == null) "None" else parent!!.data.toString()) + " | Children: " + if (getChildren().size == 0) "None" else ""
            for (child in getChildren()) {
                out += """
	        ${child.data.toString()} | Parent: ${if (child.parent == null) "None" else child.parent!!.data}"""
            }
        return out
    }
}