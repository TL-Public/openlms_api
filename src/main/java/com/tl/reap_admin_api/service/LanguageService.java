package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.LanguageDao;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.exception.LanguageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LanguageService {

    private final LanguageDao languageDao;

    @Autowired
    public LanguageService(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    @Transactional(readOnly = true)
     public Language getLanguageById(Long id) {
        return languageDao.findById(id)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Language getLanguageByCode(String code) {
        return languageDao.findByCode(code)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found with code: " + code));
    }

    @Transactional(readOnly = true)
    public List<Language> getAllLanguages() {
        return languageDao.findAll();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public Language createLanguage(Language language) {
        return languageDao.save(language);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public Language updateLanguage(Long id, Language languageDetails) {
        Language language = getLanguageById(id);
        language.setCode(languageDetails.getCode());
        language.setName(languageDetails.getName());
        return languageDao.save(language);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteLanguage(Long id) {
        Language language = getLanguageById(id);
        languageDao.delete(language);
    }
}
