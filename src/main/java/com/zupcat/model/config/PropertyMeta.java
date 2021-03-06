package com.zupcat.model.config;

import com.zupcat.audit.AuditHandlerServiceFactory;
import com.zupcat.model.DataObject;
import com.zupcat.model.DatastoreEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds metadata of busines objects properties
 */
public abstract class PropertyMeta<E> implements Serializable {

    private static final long serialVersionUID = 6181606486836703354L;
    private final DatastoreEntity owner;
    protected String name;
    protected AbstractPropertyBuilder<? extends PropertyMeta<E>, E> options;


    protected PropertyMeta(final DatastoreEntity owner) {
        this.owner = owner;
    }

    public void config(final AbstractPropertyBuilder<? extends PropertyMeta<E>, E> options) {
        this.options = options;
    }

    public String getPropertyName() {
        return name;
    }

    public void setPropertyName(final String propertyName) {
        this.name = propertyName;
    }

    protected DatastoreEntity getOwner() {
        return owner;
    }

    public boolean isFullyEquals(final PropertyMeta other) {
        // skipping owner comparisson. It is not needed and causes stackoverflow
        return !(other == null ||
                !Objects.equals(this.name, other.name) ||
                !Objects.equals(options.initialValue, other.options.initialValue) ||
                options.sendToClient != other.options.sendToClient ||
                options.auditable != other.options.auditable ||
                options.indexable != other.options.indexable);
    }

    public boolean isIndexable() {
        return options.indexable;
    }

//    public E getInitialValue() {
//        return initialValue;
//    }

    public boolean hasToSendToClient() {
        return options.sendToClient;
    }

    public E get() {
        final E result = getValueImpl(owner.getDataObject());

        return result == null ? options.initialValue : result;
    }

    public void set(final E value) {
        this.set(value, options.auditable);
    }

    public void set(final E value, final boolean forceAudit) {
        if (forceAudit) {
            AuditHandlerServiceFactory.getAuditHandler().logPropertyDataChanged(this, value, owner.getId());
        }

        final DataObject dataObject = owner.getDataObject();

        if (value == null || value.equals(options.initialValue)) {
            dataObject.remove(name);
        } else {
            setValueImpl(value, dataObject);
        }
    }

    public AbstractPropertyBuilder<? extends PropertyMeta<E>, E> getOptions() {
        return options;
    }

    protected abstract E getValueImpl(final DataObject dataObject);

    protected abstract void setValueImpl(final E value, final DataObject dataObject);

    @Override
    public String toString() {
        return "[" + StringUtils.replace(getClass().getName(), "com.zupcat.property.", "") + "|" + name + "->" + get() + "]";
    }
}
