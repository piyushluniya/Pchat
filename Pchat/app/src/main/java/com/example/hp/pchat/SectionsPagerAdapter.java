package com.example.hp.pchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hp on 11-12-2017.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter{ //FragmentPagerAdapter is a built in class

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) { //position returns the position of each tab

        switch(position)
        {
            case 0:
                RequestsFragment requestsFragment= new RequestsFragment();
                return  requestsFragment;

            case 1:
                ChatsFragment chatsFragment=new ChatsFragment();
                return  chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return  null;
        }
    }

    @Override
    public int getCount() {
        return 3; //since we are going to have 3 tabs , the count shud return 3
    }

    public CharSequence getPageTitle(int position){

        switch(position){
            case 0:
                return "REQUESTS";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }

    }
}
