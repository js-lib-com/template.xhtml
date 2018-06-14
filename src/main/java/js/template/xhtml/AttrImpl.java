package js.template.xhtml;

import js.dom.Attr;
import js.util.Strings;

/**
 * Immutable implementation for {@link Attr} interface.
 * 
 * @author Iulian Rotaru
 */
class AttrImpl implements Attr {
	/** Attribute name. */
	private final String name;
	/** Attribute value. */
	private final String value;
	/** Cache hash code for this immutable instance. */
	private final int hashCode;

	AttrImpl(String name) {
		this.name = name;
		this.value = null;
		this.hashCode = computeHashCode(this.name);
	}

	protected AttrImpl(String name, String value) {
		this.name = name;
		this.value = value;
		this.hashCode = computeHashCode(this.name);
	}

	AttrImpl(Attr attr) {
		this.name = attr.getName();
		this.value = attr.getValue();
		this.hashCode = computeHashCode(this.name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Strings.toString(name, value);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttrImpl other = (AttrImpl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	private static int computeHashCode(String name) {
		final int prime = 31;
		return prime + ((name == null) ? 0 : name.hashCode());
	}
}