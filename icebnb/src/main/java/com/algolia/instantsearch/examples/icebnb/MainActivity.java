package com.algolia.instantsearch.examples.icebnb;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.algolia.instantsearch.events.ErrorEvent;
import com.algolia.instantsearch.examples.icebnb.widgets.MapWidget;
import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.helpers.InstantSearch;
import com.algolia.search.saas.Query;
import com.google.android.gms.maps.SupportMapFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String ALGOLIA_APP_ID = "latency";
    private static final String ALGOLIA_INDEX_NAME = "airbnb";
    private static final String ALGOLIA_API_KEY = "6be0576ff61c053d5f9a3225e2a90f76";

    private Searcher searcher;

    private FilterResultsFragment filterResultsFragment;
    private SearchView searchView;
    private MapWidget mapWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        // Initialize a Searcher with your credentials and an index name
        searcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_API_KEY, ALGOLIA_INDEX_NAME);
        searcher.setQuery(new Query().setRestrictSearchableAttributes("city", "country", "name").setFacets("room_type", "price"));
        // Create the FilterResultsFragment here so it can set the appropriate facets on the Searcher
        filterResultsFragment = FilterResultsFragment.get(searcher)
                .addSeekBar("price", 100);
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        final SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        fragmentTransaction.add(R.id.map_placeholder, mapFragment);
        fragmentTransaction.commit();
        mapWidget = new MapWidget(mapFragment);
        searcher.registerResultListener(mapWidget);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        new InstantSearch(this, menu, R.id.action_search, searcher) // link the Searcher to the UI
                .search(); //Show results for empty query on startup

        final MenuItem itemSearch = menu.findItem(R.id.action_search);
        searchView = (SearchView) itemSearch.getActionView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            itemSearch.expandActionView(); //open SearchBar on startup
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.findFragmentByTag(FilterResultsFragment.TAG) == null) {
                filterResultsFragment.show(fragmentManager, FilterResultsFragment.TAG);
                hideKeyboard();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent event) {
        Toast.makeText(this, "Error searching" + event.query.getQuery() + ":" + event.error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        searchView.clearFocus();
    }
}
