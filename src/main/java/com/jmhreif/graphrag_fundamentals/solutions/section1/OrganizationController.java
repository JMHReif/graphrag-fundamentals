package com.jmhreif.graphrag_fundamentals.solutions.section1;

import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class OrganizationController {
    private final OrganizationRepository repository;

    public OrganizationController(OrganizationRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/articleMentions")
    public List<Organization> getArticleMentions() {
        return repository.findOrganizations();
    }
}
