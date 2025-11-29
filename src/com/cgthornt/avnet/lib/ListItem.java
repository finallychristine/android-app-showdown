package com.cgthornt.avnet.lib;

import java.util.Comparator;

public class ListItem implements Comparable<ListItem> {

	public String title, subtext, iconText;
	
	public Double compareItem = 0.0;
	
	public int image = -1;
	
	public ListItem(String title) {
		this(title, null);
	}
	
	public ListItem(String title, String subtext) {
		this(title, subtext, -1);
	}
	
	public ListItem(String title, String subtext, int image) {
		this.title = title;
		this.subtext = subtext;
		this.image = image;
	}
	
	public void setCompareValue(Double val) {
		compareItem = val;
	}
	
	public void setIconText(String text) {
		iconText = text;
	}

	public int compareTo(ListItem comp) {
		return compareItem.compareTo(comp.compareItem);
	}
	
	public static class ListItemComparitor implements Comparator<ListItem> {
		public int compare(ListItem lhs, ListItem rhs) {
			return rhs.compareTo(lhs);
		}
		
	}
	
}
