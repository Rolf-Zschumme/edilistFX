package de.vbl.im.tools;

public class LongForByte {
	
	private long value10; // der um den Faktor 10 erhöhte Anzeigewert
	private int faktor;   // KB=1.000 MB=1.000.000 GB=1.000.000.000
	
	public void set(long lg) {
		if (lg < 1000000) {
			faktor = 1000;
		} else if (lg < 1000000000) {
			faktor = 1000000;
		} else {
			faktor = 1000000000;
		}
		value10 = (lg * 10) / faktor;
	}
	
	public String setValue (String newValue) {
		String value10Str = "0";
		if (newValue.length() > 0) {
			try {
				Double val10 = 10 * Double.parseDouble(newValue.replace(',', '.'));
				value10Str = String.format("%.0f", val10);
			} catch (NumberFormatException ex) {
				return "Fehler: '" + newValue + "' ist keine gültige Zahl";
			}
		}
		value10 = Long.parseLong(value10Str);
		return "";
	}
	
	public long get() {
		return (value10 * faktor) / 10;
	}
	
	public String getValueStr() {
		String format = "%.0f";
		if (value10 == 0) return "";
		if (value10 < 100 && (value10 % 10) > 0 ) {
			format = "%.1f";
		} 
		double d10 = value10;
		return String.format(format, d10 / 10);
	}
	
	public String getEinheit() {
		if (value10 == 0) {
			return null;
		}
		switch (faktor) {
			case    1000: return "KB";
			case 1000000: return "MB";
			default     : return "GB";
		}	
	}
	
	public String setEinheit(String einheit) {
		if ("KB".equals(einheit))
			faktor = 1000;
		else if ("MB".equals(einheit))
			faktor = 1000000;
		else if ("GB".equals(einheit))
			faktor = 1000000000;
		else
			return "FEHLER: unbekannte Einheit '" + einheit + "'";
		return "";
	}
	
}