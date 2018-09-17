package com.allow.food4needy.adddonation

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import timber.log.Timber

/**
 * Dialog fragment that allows user to select an item from a list
 */
/**
 * Constructor
 */
class ItemPickerDialog : DialogFragment() {

    lateinit var title: String
    private lateinit var items: ArrayList<Item>
    var selectedIndex: Int = -1

    private val itemTitlesArray: Array<String>
        get() {
            return items.map { it.title }.toTypedArray()
        }

    /**
     * An item that can be displayed and selected by the ItemPickerDialogFragment
     */
    class Item(var title: String, var intValue: Int = 0, var stringValue: String = "") {

        /**
         * Construct from a bundle of values
         * @param bundle
         */
        constructor(bundle: Bundle) : this(
                bundle.getString(KEY_TITLE, null),
                bundle.getInt(KEY_INT_VALUE, 0),
                bundle.getString(KEY_STRING_VALUE, null))

        init {
            assert(!TextUtils.isEmpty(title))
        }

        /**
         * Get a Bundle of values that can be passed to the Item(Bundle) constructor
         * to re-create the object
         *
         * @return Bundle
         */
        val valuesBundle: Bundle
            get() {
                val bundle = Bundle()

                bundle.putString(KEY_TITLE, title)
                bundle.putInt(KEY_INT_VALUE, intValue)
                bundle.putString(KEY_STRING_VALUE, stringValue)

                return bundle
            }

        companion object {

            private const val KEY_TITLE = "title"
            private const val KEY_INT_VALUE = "intValue"
            private const val KEY_STRING_VALUE = "stringValue"

            /**
             * Given a list of items, create a Bundle that can be passed to
             * Item.itemsFromBundle() to recreate them.
             *
             * @param items list of items
             * @return Bundle
             */
            fun bundleOfItems(items: List<Item>): Bundle {
                val itemCount = items.size
                val itemBundles = ArrayList<Bundle>()
                for (i in 0 until itemCount) {
                    itemBundles.add(items[i].valuesBundle)
                }

                val bundle = Bundle()
                bundle.putParcelableArrayList(ARG_ITEMS, itemBundles)
                return bundle
            }

            /**
             * Given a Bundle created by Item.bundleOfItems(), recreate the
             * original list of items.
             *
             * @param bundle Bundle created by Item.bundleOfItems()
             * @return ArrayList&lt;Item&gt;
             */
            fun itemsFromBundle(bundle: Bundle): ArrayList<Item> {
                val itemBundles = bundle.getParcelableArrayList<Bundle>(ARG_ITEMS)
                val items = ArrayList<Item>()
                for (itemBundle in itemBundles!!) {
                    items.add(Item(itemBundle))
                }
                return items
            }
        }
    }

    /**
     * Interface for notification of item selection
     *
     * If the owning Activity implements this interface, then the fragment will
     * invoke its onItemSelected() method when the user clicks the OK button.
     */
    interface ItemPickerDialogInteractionListener {
        fun onItemSelected(fragment: ItemPickerDialog, item: Item, index: Int)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(ARG_SELECTED_INDEX, selectedIndex)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        if (args != null) {
            title = args.getString(ARG_TITLE, "Dialog")
            items = Item.itemsFromBundle(args.getBundle(ARG_ITEMS)!!)
            selectedIndex = args.getInt(ARG_SELECTED_INDEX, -1)
        }

        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(ARG_SELECTED_INDEX, selectedIndex)
        }

        val itemTitles = itemTitlesArray

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Timber.d("OK button clicked")

                    if (parentFragment is ItemPickerDialogInteractionListener) {
                        if (0 <= selectedIndex && selectedIndex < items.size) {
                            val item = items[selectedIndex]
                            (parentFragment as ItemPickerDialogInteractionListener)
                                    .onItemSelected(this@ItemPickerDialog, item, selectedIndex)
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    Timber.d("Cancel button clicked")

                    // OK, just let the dialog be closed
                }
                .setSingleChoiceItems(itemTitles, selectedIndex) { _, which ->
                    Timber.d("User clicked item with index $which")
                    selectedIndex = which
                }

        return builder.create()
    }

    companion object {
        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_ITEMS = "ARG_ITEMS"
        const val ARG_SELECTED_INDEX = "ARG_SELECTED_INDEX"

        /**
         * Create a new instance of ItemPickerDialogFragment with specified arguments
         *
         * @param title Dialog title text
         * @param items Selectable items
         * @param selectedIndex initial selection index, or -1 if no item should be pre-selected
         * @return ItemPickerDialogFragment
         */
        fun newInstance(title: String, items: List<Item>, selectedIndex: Int): ItemPickerDialog {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putBundle(ARG_ITEMS, Item.bundleOfItems(items))
            args.putInt(ARG_SELECTED_INDEX, selectedIndex)

            val fragment = ItemPickerDialog()
            fragment.arguments = args
            return fragment
        }
    }
}

