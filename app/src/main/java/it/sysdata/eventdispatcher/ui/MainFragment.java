/*
 * Copyright (C) 2020 Sysdata Digital, S.r.l.
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

package it.sysdata.eventdispatcher.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baseandroid.events.EventDispatcher;
import com.baseandroid.events.rx.annotations.RxSubscribe;

import androidx.fragment.app.Fragment;
import it.sysdata.eventdispatcher.R;
import it.sysdata.eventdispatcher.events.UIEvent;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

	public MainFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		// register to events
		EventDispatcher.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// unregister from incoming events
		EventDispatcher.unregister(this);
	}

	@RxSubscribe
	public void onConsumeUIEvent(final UIEvent uiEvent) {
		Toast.makeText(getActivity(), "received UIEvent", Toast.LENGTH_SHORT).show();
	}
}
