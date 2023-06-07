package com.pupu.pu1611.listeners;

import com.pupu.pu1611.models.User;

public interface UserListener {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

}
