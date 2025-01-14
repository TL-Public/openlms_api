package com.tl.reap_admin_api.util;

import com.tl.reap_admin_api.dto.Cloneable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TranslationUtil {

    public static <T extends Cloneable<T>> List<T> addEnglishFallbackForHindi(List<T> translations, 
                                                         String languageCodeGetter, 
                                                         String languageCodeSetter) {
        Map<String, T> translationMap = translations.stream()
                .collect(Collectors.toMap(
                    t -> (String) getFieldValue(t, languageCodeGetter),
                    t -> t,
                    (existing, replacement) -> existing
                ));

        if (!translationMap.containsKey("hi") && translationMap.containsKey("en")) {
            T englishTranslation = translationMap.get("en");
            T hindiTranslation = englishTranslation.deepClone();
            setFieldValue(hindiTranslation, languageCodeSetter, "hi");
            translationMap.put("hi", hindiTranslation);
        }

        return List.copyOf(translationMap.values());
    }

    private static Object getFieldValue(Object object, String fieldName) {
        try {
            return object.getClass().getMethod(fieldName).invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value", e);
        }
    }

    private static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            object.getClass().getMethod(fieldName, value.getClass()).invoke(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value", e);
        }
    }
}