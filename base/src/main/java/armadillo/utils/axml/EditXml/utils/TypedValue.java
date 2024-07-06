package armadillo.utils.axml.EditXml.utils;

/**
 * @author Dmitry Skiba
 *
 */
public class TypedValue {

    public int type;
    public CharSequence string;
    public int data;
    public int assetCookie;
    public int resourceId;
    public int changingConfigurations;
	/** The value contains no data. */
	public static final int TYPE_NULL = 0x00;

	/** The <var>data</var> field holds a resource identifier. */
	public static final int TYPE_REFERENCE = 0x01;
	/** The <var>data</var> field holds an attribute resource
	 *  identifier (referencing an attribute in the current theme
	 *  style, not a resource entry). */
	public static final int TYPE_ATTRIBUTE = 0x02;
	/** The <var>string</var> field holds string data.  In addition, if
	 *  <var>data</var> is non-zero then it is the string block
	 *  index of the string and <var>assetCookie</var> is the set of
	 *  assets the string came from. */
	public static final int TYPE_STRING = 0x03;
	/** The <var>data</var> field holds an IEEE 754 floating point number. */
	public static final int TYPE_FLOAT = 0x04;
	/** The <var>data</var> field holds a complex number encoding a
	 *  dimension value. */
	public static final int TYPE_DIMENSION = 0x05;
	/** The <var>data</var> field holds a complex number encoding a fraction
	 *  of a container. */
	public static final int TYPE_FRACTION = 0x06;

	/** Identifies the start of plain integer values.  Any type value
	 *  from this to {@link #TYPE_LAST_INT} means the
	 *  <var>data</var> field holds a generic integer value. */
	public static final int TYPE_FIRST_INT = 0x10;

	/** The <var>data</var> field holds a number that was
	 *  originally specified in decimal. */
	public static final int TYPE_INT_DEC = 0x10;
	/** The <var>data</var> field holds a number that was
	 *  originally specified in hexadecimal (0xn). */
	public static final int TYPE_INT_HEX = 0x11;
	/** The <var>data</var> field holds 0 or 1 that was originally
	 *  specified as "false" or "true". */
	public static final int TYPE_INT_BOOLEAN = 0x12;

	/** Identifies the start of integer values that were specified as
	 *  color constants (starting with '#'). */
	public static final int TYPE_FIRST_COLOR_INT = 0x1c;

	/** The <var>data</var> field holds a color that was originally
	 *  specified as #aarrggbb. */
	public static final int TYPE_INT_COLOR_ARGB8 = 0x1c;
	/** The <var>data</var> field holds a color that was originally
	 *  specified as #rrggbb. */
	public static final int TYPE_INT_COLOR_RGB8 = 0x1d;
	/** The <var>data</var> field holds a color that was originally
	 *  specified as #argb. */
	public static final int TYPE_INT_COLOR_ARGB4 = 0x1e;
	/** The <var>data</var> field holds a color that was originally
	 *  specified as #rgb. */
	public static final int TYPE_INT_COLOR_RGB4 = 0x1f;

	/** Identifies the end of integer values that were specified as color
	 *  constants. */
	public static final int TYPE_LAST_COLOR_INT = 0x1f;

	/** Identifies the end of plain integer values. */
	public static final int TYPE_LAST_INT = 0x1f;

    public static final int
	    COMPLEX_UNIT_PX			=0,
	    COMPLEX_UNIT_DIP		=1,
	    COMPLEX_UNIT_SP			=2,
	    COMPLEX_UNIT_PT			=3,
	    COMPLEX_UNIT_IN			=4,
	    COMPLEX_UNIT_MM			=5,
    	COMPLEX_UNIT_SHIFT		=0,
	    COMPLEX_UNIT_MASK		=15,
	    COMPLEX_UNIT_FRACTION	=0,
	    COMPLEX_UNIT_FRACTION_PARENT=1,
	    COMPLEX_RADIX_23p0		=0,
	    COMPLEX_RADIX_16p7		=1,
	    COMPLEX_RADIX_8p15		=2,
	    COMPLEX_RADIX_0p23		=3,
	    COMPLEX_RADIX_SHIFT		=4,
	    COMPLEX_RADIX_MASK		=3,
	    COMPLEX_MANTISSA_SHIFT	=8,
	    COMPLEX_MANTISSA_MASK	=0xFFFFFF;

}

