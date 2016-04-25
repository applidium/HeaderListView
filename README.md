# DEPRECATED

HeaderListView is deprecated. No new development will be taking place.

# Quickstart

1. Import the HeaderListView module in your Android Studio project.
2. Replace your `ListView` with `HeaderListView`
3. Implement a subclass of `SectionAdapter`
4. Set it to your `HeaderListView` with `setAdapter(SectionAdapter adapter)`

# HeaderListView

`HeaderListView` is a list view with sections and with a cool iOS-like "sticky" section headers. Notice that `HeaderListView` is not a subclass of Android's `ListView` but uses composition. Hence, you will need to call `getListView()` to access the underlying `ListView`. 
