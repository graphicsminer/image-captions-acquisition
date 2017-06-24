package the.miner.activity.fragment;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

import the.miner.activity.adapter.GMBaseAdapter;

public abstract class GMBaseFragment<T extends GMBaseAdapter<?>> extends Fragment {

    private Serializable mData;
    private T adapter;

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = initAdapter(view.getContext());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get data
     *
     * @return data
     */
    public Serializable getData() {
        return mData;
    }

    /**
     * Set data
     *
     * @param data data
     */
    public void setData(Serializable data) {
        this.mData = data;
    }

    /**
     * Get adapter
     *
     * @return adapter
     */
    public T getAdapter() {
        return adapter;
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Create new adapter
     *
     * @param context activity context
     */
    protected abstract T initAdapter(Context context);
}
