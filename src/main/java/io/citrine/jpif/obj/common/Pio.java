package io.citrine.jpif.obj.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.citrine.jpif.obj.merge.MergeStrategy;
import io.citrine.jpif.obj.merge.PioReflection;
import io.citrine.jpif.util.PifObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all Physical Information Objects.
 *
 * @author Kyle Michel
 * @author Sean Paradiso
 */
public abstract class Pio {

    /**
     * Set the tags for this object.
     *
     * @param tags List of strings with the tags of this object.
     */
    @JsonSetter(value = "tags")
    protected void setTags(final List<String> tags) {  // Private since only Jackson should use it
        this.tags = tags;
    }

    /**
     * Set the tags for this object.
     *
     * @param tag List of strings with the tags of this object.
     */
    @JsonSetter(value = "tag")
    protected void setTag(final List<String> tag) {  // Private since only Jackson should use it
        setTags(tag);
    }

    /**
     * Add to the tags for this object.
     *
     * @param tag String with the tag to add.
     * @return This object.
     */
    public Pio addTag(final String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
        return this;
    }

    /**
     * Insert a tag at the set index.
     *
     * @param index Index at which to input the tag.
     * @param tag   String with the tag to add.
     * @return This object.
     */
    public Pio addTag(final int index, final String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(index, tag);
        return this;
    }

    /**
     * Remove a tag from this object.
     *
     * @param tag String with the tag to remove.
     * @return True if the tag was removed.
     */
    public boolean removeTag(final String tag) {
        return (this.tags != null) && this.tags.remove(tag);
    }

    /**
     * Get the number of tags attached to this object.
     *
     * @return Number of tags for this object.
     */
    public int numTags() {
        return (this.tags == null) ? 0 : this.tags.size();
    }

    /**
     * Get the tag at the set index.
     *
     * @param index Index of the tag to return.
     * @return String with the tag at the input index.
     * @throws IndexOutOfBoundsException if the input is out of range of the tags list.
     */
    @JsonIgnore
    public String getTag(final int index) {
        if (this.tags == null) {
            throw new IndexOutOfBoundsException("Attempting to access tag " + index + " of " + this.numTags());
        }
        return this.tags.get(index);
    }

    /**
     * Get an iterable over tags.
     *
     * @return Iterable object over tags for this object.
     */
    public Iterable<String> tags() {
        return (this.tags == null) ? Collections.emptyList() : this.tags;
    }

    /**
     * Get the list of tags.
     *
     * @return List of tags for this object.
     */
    @JsonGetter("tags")
    protected List<String> getTags() { // Private since only Jackson should use it
        return this.tags;
    }

    /**
     * Add an unsupported field to this object.
     *
     * @param key   String with the key of the field.
     * @param value Object with the value of the field.
     * @return This object.
     */
    @JsonAnySetter
    public Pio addUnsupportedField(final String key, final Object value) {
        if (this.unsupportedFields == null) {
            this.unsupportedFields = new HashMap<>();
        }
        this.unsupportedFields.put(key, value);
        return this;
    }

    /**
     * Get the map of all unsupported fields.
     *
     * @return Map of strings to objects with the unsupported fields.
     */
    @JsonAnyGetter
    protected Map<String, Object> getUnsupportedFields() { // Private since only Jackson should use it
        return this.unsupportedFields;
    }

    /**
     * Get the number of unsupported fields.
     *
     * @return Number of unsupported fields.
     */
    public int numUnsupportedFields() {
        return (this.unsupportedFields == null) ? 0 : this.unsupportedFields.size();
    }

    /**
     * Return whether there is an unsupported field with the input key.
     *
     * @param key Key to check whether in unsupported fields.
     * @return True if the key exists in the unsupported fields.
     */
    public boolean containsUnsupportedFieldKey(final String key) {
        return (this.unsupportedFields != null) && this.unsupportedFields.containsKey(key);
    }

    /**
     * Get an {@link Iterable} object to iterate over key/value pairs of the unsupported fields.
     *
     * @return {@link Iterable} object for the unsupported fields.
     */
    public Iterable<Map.Entry<String, Object>> unsupportedFields() {
        return (this.unsupportedFields == null) ? Collections.emptySet() : this.unsupportedFields.entrySet();
    }

    /**
     * Get the value of an unsupported field.
     *
     * @param key String with the key of the field.
     * @return Object with the value of the field or a null pointer if the field does not exist.
     */
    @JsonIgnore
    public Object getUnsupportedFieldValue(final String key) {
        return (this.unsupportedFields == null) ? null : this.unsupportedFields.get(key);
    }

    /**
     * Remove a single unsupported field by its key value (if present).
     *
     * @param key String with the key of the unsupported field to remove.
     */
    public void removeUnsupportedField(final String key) {
        if (this.unsupportedFields != null) {
            this.unsupportedFields.remove(key);
        }
    }

    /**
     * Remove all unsupported fields.
     */
    public void clearUnsupportedFields() {
        if (this.unsupportedFields != null) {
            this.unsupportedFields.clear();
        }
    }

    /**
     * Default mergePio behavior on a field-by-field basis.
     *
     * @param reflection      a pre-computed PioReflection for the Pio type being merged.
     * @param fieldGetterName the getter for the field being merged. (ex. "getComposition").
     * @param mergeFrom       the Pio instance to mergePio from.
     * @param strategy        the mergePio strategy to use.
     * @return the merged Pio instance.
     */
    protected Pio merge(PioReflection reflection, String fieldGetterName, Pio mergeFrom, MergeStrategy strategy)
            throws InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Method getter = reflection.getters().apply(fieldGetterName);
        java.lang.reflect.Method setter = reflection.setters().apply(fieldGetterName.replace("get", "set"));

        // If the type to mergePio is a List
        if (reflection.getters().apply(fieldGetterName).getReturnType().getCanonicalName().equals("java.util.List")) {
            List<Object> thisList = (List<Object>) getter.invoke(this);
            List<Object> mergeFromList = (List<Object>) getter.invoke(mergeFrom);
            List<Object> result = strategy.mergeLists(thisList, mergeFromList);

            setter.invoke(this, result);
        } else {
            Object thisObj = getter.invoke(this);
            Object fromObj = getter.invoke(mergeFrom);
            setter.invoke(this, strategy.merge(thisObj, fromObj));
        }

        return this;
    }

    /**
     * Merge another Pio object into `this`, using a specified mergePio strategy.
     *
     * @param mergeFrom Pio object to mergePio into `this`.
     * @return the result of the mergePio as a new Pio object.
     * @throws IOException if jackson fails to de/serialize the Pio instance during deep copy.
     */
    public Pio merge(Pio mergeFrom, MergeStrategy strategy) throws Exception {
        assert (mergeFrom.getClass() == this.getClass());

        Pio mergeResult = PifObjectMapper.deepCopy(this, this.getClass());
        PioReflection reflection = new PioReflection(this);

        List<String> ignoredFields = Arrays.asList("getClass");

        // Iterate over getter/setter pairs and mergePio each field.
        reflection.getGetterFieldKeys()
                .stream()

                // Skip ignored fields
                .filter(getter -> {
                    java.lang.reflect.Method method = reflection.getMethod(getter);
                    return !ignoredFields.stream()
                            .map(k -> method.getName().equalsIgnoreCase(k)).reduce((a, b) -> a || b)
                            .orElse(false);
                })

                // Merge each remaining field
                .forEach(getter -> {
                            try {
                                mergeResult.merge(reflection, getter, mergeFrom, strategy);
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                );
        return mergeResult;
    }

    /**
     * List of tags for the object.
     */
    private List<String> tags;

    /**
     * Map of unsupported field names to their values.
     */
    private Map<String, Object> unsupportedFields;
}