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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baseandroid.events.EventDispatcher;
import com.squareup.otto.Subscribe;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

	public MainActivityFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		EventDispatcher.register(this);
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Subscribe
	public void onConsumePippo(final UIEvent UIEvent) {
		Toast.makeText(getActivity(), "received UIEvent", Toast.LENGTH_SHORT).show();
	}
}
