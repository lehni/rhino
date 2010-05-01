package org.mozilla.javascript;

public class PropertyDescriptor {
    private Boolean enumerable;
    private Boolean configurable;
    private Object value;
    private Boolean writable;
    private Object getter;
    private Object setter;
    private boolean hasValue;
    private boolean hasGetter;
    private boolean hasSetter;

    // Controls whether the NativeObject produced in toObject should be extensible
    private boolean extensible;

    public PropertyDescriptor(Object value, boolean writable,
            boolean enumerable, boolean configurable, boolean extensible) {
        this.value = value;
        this.writable = writable;
        this.enumerable = enumerable;
        this.configurable = configurable;
        this.extensible = extensible;
        hasValue = true;
    }

    public PropertyDescriptor(Object getter, Object setter, boolean enumerable,
            boolean configurable, boolean extensible) {
        this.getter = getter;
        this.setter = setter;
        this.enumerable = enumerable;
        this.configurable = configurable;
        this.extensible = extensible;
        hasGetter = getter != null;
        hasSetter = setter != null;
    }

    public PropertyDescriptor(Object value, int attributes) {
        this(value == null ? Undefined.instance : value, 
            (attributes & ScriptableObject.READONLY) == 0,
            (attributes & ScriptableObject.DONTENUM) == 0,
            (attributes & ScriptableObject.PERMANENT) == 0, true);
    }

    public PropertyDescriptor(Object getter, Object setter, int attributes) {
        this(getter, setter,
            (attributes & ScriptableObject.DONTENUM) == 0,
            (attributes & ScriptableObject.PERMANENT) == 0, true);
    }

    public PropertyDescriptor(Object obj) {
        // Implementation of ToPropertyDescriptor, 8.10.5, ECMA-262-5
        if (!(obj instanceof Scriptable))
            throw ScriptRuntime.typeError1("msg.arg.not.object",
                    ScriptRuntime.typeof(obj));
        Scriptable scriptable = (Scriptable) obj;
        Object enumerable = ScriptableObject.getProperty(scriptable,
                "enumerable");
        if (enumerable != Scriptable.NOT_FOUND)
            this.enumerable = ScriptRuntime.toBoolean(enumerable);
        
        Object configurable = ScriptableObject.getProperty(scriptable,
                "configurable");
        if (configurable != Scriptable.NOT_FOUND)
            this.configurable = ScriptRuntime.toBoolean(configurable);

        Object value = ScriptableObject.getProperty(scriptable, "value");
        if (value != Scriptable.NOT_FOUND) {
            this.value = value;
            hasValue = true;
        }

        Object writable = ScriptableObject.getProperty(scriptable, "writable");
        if (writable != Scriptable.NOT_FOUND)
            this.writable = ScriptRuntime.toBoolean(writable);

        Object get = ScriptableObject.getProperty(scriptable, "get");
        if (get != Scriptable.NOT_FOUND) {
            if (get != Undefined.instance && !(get instanceof Callable))
                throw ScriptRuntime.notFunctionError(get);
            this.getter = get;
            hasGetter = true;
        }

        Object set = ScriptableObject.getProperty(scriptable, "set");
        if (set != Scriptable.NOT_FOUND) {
            if (set != Undefined.instance && !(set instanceof Callable))
                throw ScriptRuntime.notFunctionError(set);
            this.setter = set;
            hasSetter = true;
        }

        if (hasGetter || hasSetter) {
            if (hasValue || this.writable != null)
                throw ScriptRuntime.typeError0(
                        "msg.both.data.and.accessor.desc");
        }

        extensible = true;
    }

    public boolean isEnumerable() {
        return enumerable != null && enumerable;
    }

    public void setEnumerable(boolean enumerable) {
        this.enumerable = enumerable;
    }

    public boolean isConfigurable() {
        return configurable != null && configurable;
    }

    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        hasValue = true;
    }

    public boolean isWritable() {
        return writable != null && writable;
    }

    public void setWritable(Boolean writable) {
        this.writable = writable;
    }

    public Object getGetter() {
        return getter;
    }

    public void setGetter(Object getter) {
        this.getter = getter;
        hasGetter = true;
    }

    public Object getSetter() {
        return setter;
    }

    public void setSetter(Object setter) {
        this.setter = setter;
        hasSetter = true;
    }

    public boolean isDataDescriptor() {
        return hasValue || writable != null;
    }

    public boolean isAccessorDescriptor() {
        return hasGetter || hasSetter;
    }

    public boolean isGenericDescriptor() {
        return !isDataDescriptor() && !isAccessorDescriptor();
    }

    public boolean hasValue() {
        return hasValue;
    }

    public boolean hasGetter() {
        return hasGetter;
    }
    
    public boolean hasSetter() {
        return hasSetter;
    }

    public int applyToAttributes(int attributes) {
        if (enumerable != null) {
            attributes = enumerable
                    ? attributes & ~ScriptableObject.DONTENUM
                    : attributes | ScriptableObject.DONTENUM;
        }
        if (writable != null) {
            attributes = writable
                    ? attributes & ~ScriptableObject.READONLY
                    : attributes | ScriptableObject.READONLY;
        }
        if (configurable != null) {
            attributes = configurable
                    ? attributes & ~ScriptableObject.PERMANENT
                    : attributes | ScriptableObject.PERMANENT;
        }
        return attributes;
    }

    public NativeObject toObject(Scriptable scope) {
        // 8.10.4 FromPropertyDescriptor
        NativeObject obj = new NativeObject();
        ScriptRuntime.setObjectProtoAndParent(obj, scope);
        if (isDataDescriptor()) {
            if (hasValue)
                obj.defineProperty("value", value, ScriptableObject.EMPTY);
            if (writable != null)
                obj.defineProperty("writable", writable,
                        ScriptableObject.EMPTY);
        } else if (isAccessorDescriptor()) {
            if (hasGetter)
                obj.defineProperty("get", getter, ScriptableObject.EMPTY);
            if (hasSetter)
                obj.defineProperty("set", setter, ScriptableObject.EMPTY);
        }
        if (enumerable != null)
            obj.defineProperty("enumerable", enumerable,
                    ScriptableObject.EMPTY);
        if (configurable != null)
            obj.defineProperty("configurable", configurable,
                    ScriptableObject.EMPTY);
        if (!extensible)
            obj.preventExtensions();
        return obj;
    }


    protected static boolean changes(Object currentValue, Object newValue) {
        if (newValue == Scriptable.NOT_FOUND)
            return false;
        if (currentValue == Scriptable.NOT_FOUND)
            currentValue = Undefined.instance;
        return !ScriptRuntime.shallowEq(currentValue, newValue);
    }
}
