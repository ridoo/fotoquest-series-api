/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.series.dwd.srv.v1;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v1.CategoryOutput;
import org.n52.io.response.v1.CategoryOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.dwd.rest.VorabInformationAlert;
import org.n52.series.dwd.rest.WarnungAlert;
import org.n52.series.dwd.store.AlertStore;
import org.n52.web.ctrl.v1.ext.ExtUrlSettings;
import org.n52.web.ctrl.v1.ext.UrlHelper;

public class CategoryOutputAdapter extends ParameterService<CategoryOutput> {

    private final AlertStore store;

    private final UrlHelper urlHelper = new UrlHelper();

    public CategoryOutputAdapter(AlertStore store) {
        this.store = store;
    }

    private OutputCollection<CategoryOutput> createOutputCollection() {
        return new OutputCollection<CategoryOutput>() {
            @Override
            protected Comparator<CategoryOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public OutputCollection<CategoryOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<CategoryOutput> outputCollection = createOutputCollection();
        for (String alertType : store.getAlertTypes()) {
            outputCollection.addItem(createExpanded(alertType, query));
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<CategoryOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<CategoryOutput> outputCollection = createOutputCollection();
        for (String category : getCategory()) {
            outputCollection.addItem(createCondensed(category, query));
        }
        return outputCollection;
    }

    private Set<String> getCategory() {
        Set<String> offerings = new HashSet<String>();
        if (store.getCurrentAlerts().hasWarning()) {
            Map<String, List<WarnungAlert>> warnings = store.getCurrentAlerts().getWarnings();
            for (List<WarnungAlert> list : warnings.values()) {
                for (WarnungAlert warnungAlert : list) {
                    offerings.add(warnungAlert.getEvent());
                }
            }
        }
        if (store.getCurrentAlerts().hasVorabInformation()) {
            Map<String, List<VorabInformationAlert>> vorabInformation = store.getCurrentAlerts().getVorabInformation();
            for (List<VorabInformationAlert> list : vorabInformation.values()) {
                for (VorabInformationAlert vorabInformationAlert : list) {
                    offerings.add(vorabInformationAlert.getEvent());
                }
            }
        }
        return offerings;
    }

    @Override
    public OutputCollection<CategoryOutput> getParameters(String[] items, IoParameters query) {
        OutputCollection<CategoryOutput> outputCollection = createOutputCollection();
        for (String alertType : items) {
            if (query.isExpanded()) {
                outputCollection.addItem(createExpanded(alertType, query));
            } else {
                outputCollection.addItem(createExpanded(alertType, query));
            }
        }
        return outputCollection;
    }

    @Override
    public CategoryOutput getParameter(String item, IoParameters query) {
        return createExpanded(item, query);
    }

    private CategoryOutput createCondensed(String item, IoParameters query) {
        CategoryOutput result = new CategoryOutput();
        result.setLabel(item);
        result.setId(item);
        result.setDomainId(item);
        checkForHref(result, query);
        return result;
    }

    private CategoryOutput createExpanded(String item, IoParameters query) {
        CategoryOutput result = createCondensed(item, query);
//        result.setService(getServiceOutput());
        return result;
    }

    @Override
    public boolean exists(String id) {
        return getCategory().contains(id);
    }

    private void checkForHref(CategoryOutput result, IoParameters parameters) {
        if (parameters.getHrefBase() != null && parameters.getHrefBase().contains(ExtUrlSettings.EXT)) {
            result.setHrefBase(urlHelper.getOfferingsHrefBaseUrl(parameters.getHrefBase()));
        }
    }

}
