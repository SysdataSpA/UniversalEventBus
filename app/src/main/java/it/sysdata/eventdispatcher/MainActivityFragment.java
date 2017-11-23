/*
 * Copyright (C) 2016 Sysdata Digital, S.r.l.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.sysdata.eventdispatcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baseandroid.events.EventDispatcher;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
	private final static String LOG_TAG = MainActivityFragment.class.getSimpleName();

	public MainActivityFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		EventDispatcher.register(this);
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	/**
	 * This method will be called when a UIEvent is posted on Otto Event Bus
	 * @param uiEvent
	 */
	//@Subscribe
	//public void onConsumeUIEvent(UIEvent uiEvent) {
	//	Toast.makeText(getActivity(), "Otto Subscribe: received UIEvent", Toast.LENGTH_SHORT).show();
	//}

	/**
	 * This method will be called when a UIEvent is posted on Rx Event Bus
	 * @param uiEvent
	 */
	//@RxSubscribe
	//public void onConsumeUIEvent(UIEvent uiEvent) {
	//	Toast.makeText(getActivity(), "RxSubscribe: received UIEvent", Toast.LENGTH_SHORT).show();
	//}

	/**
	 * This method will be called when a UIEvent is posted on GreenRobot Event Bus
	 * @param uiEvent
	 */
	@Subscribe(threadMode = ThreadMode.MAIN_ORDERED, priority = 2)
	public void onConsumeUIEvent(UIEvent uiEvent) {
		Log.d(LOG_TAG, "GreenRobot Subscribe: received UIEvent");
		Toast.makeText(getActivity(), "GreenRobot Subscribe: received UIEvent", Toast.LENGTH_SHORT).show();
	}

}
