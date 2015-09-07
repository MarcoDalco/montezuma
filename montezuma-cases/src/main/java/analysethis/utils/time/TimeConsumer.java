package analysethis.utils.time;

import java.util.Calendar;
import java.util.Date;

public class TimeConsumer {
	public Date getDate() {
		return new Date();
	}

	public long getTimeMillis() {
		return System.currentTimeMillis();
	}

	public Calendar getCalendar() {
		return Calendar.getInstance();
	}

	public long getNanos() {
		return System.nanoTime();
	}

	public java.sql.Date getSQLDate() {
		return new java.sql.Date(System.currentTimeMillis());
	}
}
