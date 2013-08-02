Quickstart
==========

  1. Import the project in Eclipse then add it to the build path of your project.
  2. Replace your `ListView` with `HeaderListView`
  3. Implement a subclass of `SectionAdapter`
  4. Set it to your `HeaderListView` with `setAdapter(SectionAdapter adapter)`

HeaderListView
==============

`HeaderListView` is a list view with sections and with a cool iOS-like "sticky" section headers. Notice that `HeaderListView` is not a subclass of Android's `ListView` but uses composition. Hence, you will need to call `getListView()` to access the underlying `ListView`. 


Public API
----------

~~~java
//Constructors
HeaderListView(Context)
HeaderListView(Context, AttributeSet)
//Getters
ListView getListView()
//Setters
void setAdapter(SectionAdapter)
~~~

SectionAdapter
==============

`SectionAdapter` is a subclass of `BaseAdapter` that adds the concept of sections. It can be used independently of `HeaderListView` with any subclass of `ListView`.

Public API
----------

~~~java
abstract int numberOfSections()
abstract int numberOfRows(int)
abstract View getRowView(int, int, View, ViewGroup)
abstract Object getRowItem(int, int)
boolean hasSectionHeaderView(int)
View getSectionHeaderView(int, View, ViewGroup)
Object getSectionHeaderItem(int)
int getRowViewTypeCount()
int getRowItemViewType(int, int)
int getSectionHeaderViewTypeCount()
int getSectionHeaderItemViewType(int)
void onRowItemClick(AdapterView<?>, View, int, int, long)
~~~

Future work
===========

  - Handle the case where a section has no header
  - Handle the case where the ListView has a header view
  - Handle listViews with fast scroll
  - Pass ListView XML attributes to the mListView
  - See if there are methods to dispatch to mListView
