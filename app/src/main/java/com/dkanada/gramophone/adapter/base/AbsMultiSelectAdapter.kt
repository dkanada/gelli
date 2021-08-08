package com.dkanada.gramophone.adapter.base

import android.app.Activity
import android.content.Context
import android.view.MenuItem
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.attached.isDestroyed
import com.afollestad.materialcab.createCab
import com.dkanada.gramophone.R
import com.dkanada.gramophone.interfaces.CabHolder
import com.dkanada.gramophone.util.PreferenceUtil
import java.util.*

abstract class AbsMultiSelectAdapter<VH : RecyclerView.ViewHolder, I>(
    private val context: Context,
    private val viewRes: Int,
    private var menuRes: Int,
) : RecyclerView.Adapter<VH>() {
    private var checked: MutableList<I> = ArrayList()
    private var cab: AttachedCab? = null

    protected fun isChecked(identifier: I): Boolean {
        return checked.contains(identifier)
    }

    protected fun isActive(): Boolean {
        return cab.isActive()
    }

    protected fun toggleChecked(position: Int): Boolean {
        val identifier = getIdentifier(position)

        if (identifier != null && !checked.remove(identifier)) {
            checked.add(identifier)
        }

        notifyItemChanged(position)
        updateCab()

        return true
    }

    protected fun setMenu(menuRes: Int) {
        this.menuRes = menuRes
    }

    private fun updateCab() {
        when {
            cab == null || cab.isDestroyed() -> {
                val activity = context as Activity
                val cabHolder = context as CabHolder

                cab = activity.createCab(viewRes) {
                    menu(menuRes)

                    backgroundColor(literal = PreferenceUtil.getInstance(context).primaryColor)
                    title(literal = getName(checked[0]))

                    onCreate { cab, _ -> cabHolder.onCreateCab(cab) }
                    onSelection { item -> onSelectionCab(item) }
                    onDestroy { onDestroyCab() }
                }
            }
            checked.size <= 0 && cab.isActive() -> {
                cab.destroy()
            }
            checked.size == 1 -> {
                cab?.title(literal = getName(checked[0]))
            }
            else -> {
                cab?.title(literal = context.getString(R.string.x_selected, checked.size))
            }
        }
    }

    private fun checkAll() {
        checked.clear()

        for (i in 0 until itemCount) {
            val identifier = getIdentifier(i)

            if (identifier != null) {
                checked.add(identifier)
            }
        }

        notifyDataSetChanged()
        updateCab()
    }

    private fun checkNone() {
        checked.clear()
        notifyDataSetChanged()
    }

    private fun onSelectionCab(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_select_all) {
            checkAll()
        } else {
            onMultipleItemAction(menuItem, ArrayList(checked))
            cab.destroy()
        }

        return true
    }

    private fun onDestroyCab(): Boolean {
        checkNone()

        return true
    }

    protected abstract fun getName(identifier: I): String?

    protected abstract fun getIdentifier(position: Int): I?

    @JvmSuppressWildcards
    protected abstract fun onMultipleItemAction(menuItem: MenuItem, selection: List<I>)
}
