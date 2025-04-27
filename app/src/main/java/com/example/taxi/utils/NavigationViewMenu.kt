package com.example.taxi.utils

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.navigation.NavigationView

fun addGroupsToNavigationView(
    navigationView: NavigationView,
    groups: List<Triple<Int, String, List<Triple<Int, String, Int>>>>,
    itemActionViews: Map<Int, View>
) {
    val menu = navigationView.menu

    for (groupData in groups) {
        val groupId = groupData.first
        val groupTitle = groupData.second
        val groupItems = groupData.third

        val group = menu.addSubMenu(Menu.NONE, groupId, 0,groupTitle)

        menu.setGroupVisible(groupId, false)

        for (item in groupItems) {
            val itemId = item.first
            val itemTitle = item.second
            val itemIcon = item.third
            val menuItem = menu.add(groupId, itemId, Menu.NONE, itemTitle)

//            val menuItem = group.add(Menu.NONE, itemId, Menu.NONE, itemTitle)
            menuItem.icon = ContextCompat.getDrawable(navigationView.context, itemIcon)

            if (itemActionViews.containsKey(itemId)) {
                menuItem.actionView = itemActionViews[itemId]
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
            // Set the action view to the menu item
//            menuItem.actionView = actionView

            // Set any necessary listeners or modifications to the action view
            // For example:
//            val actionViewButton = actionView.findViewById<Button>(R.id.actionViewButton)
//            actionViewButton.setOnClickListener {
//                // Handle action view button click
//            }
        }
    }

    navigationView.invalidate()
}
