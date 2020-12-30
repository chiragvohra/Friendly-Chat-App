package com.example.friendlychat;

//import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.firebase.database.annotations.Nullable;

public class TabsAccessorAdapter extends FragmentPagerAdapter {


    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0 :
                        ChatsFragment chatsFragment = new ChatsFragment();
                        return chatsFragment;
            case 1 :
                        GroupsFragment groupsFragment = new GroupsFragment();
                        return groupsFragment;
//            case 2 :
//                        ContactsFragment contactsFragment= new ContactsFragment();
//                        return contactsFragment;
            case 2 :
                        RequestsFragment requestsFragment= new RequestsFragment();
                        return requestsFragment;


            default : return null;



        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0 : return "CHATS";
            case 1 : return "GROUPS";
//            case 2 : return "CONTACTS";
            case 2 : return "REQUESTS";


            default : return null;

        }
    }
}
