/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.platform.forms.layout.functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.Layout;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRow;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRowDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetReference;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetSelectOption;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetTypeDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.service.LayoutStore;
import org.nuxeo.runtime.api.Framework;

/**
 * Provides helper methods, declared as static, to be used by the rendering framework.
 *
 * @since 5.5
 */
public class LayoutFunctions {

    public static WidgetTypeDefinition getWidgetTypeDefinition(String category, String typeName) {
        LayoutStore layoutService = Framework.getService(LayoutStore.class);
        return layoutService.getWidgetTypeDefinition(category, typeName);
    }

    /**
     * Returns a String representing each of the field definitions property name, separated by a space.
     */
    public static String getFieldDefinitionsAsString(FieldDefinition[] defs) {
        StringBuilder buff = new StringBuilder();
        if (defs != null) {
            for (FieldDefinition def : defs) {
                buff.append(def.getPropertyName()).append(" ");
            }
        }
        return buff.toString().trim();
    }

    public static List<LayoutRow> getSelectedRows(Layout layout, List<String> selectedRowNames,
            boolean showAlwaysSelected) {
        LayoutRow[] rows = layout.getRows();
        List<LayoutRow> selectedRows = new ArrayList<LayoutRow>();
        if (rows != null) {
            for (LayoutRow row : rows) {
                if (row.isAlwaysSelected() && showAlwaysSelected) {
                    selectedRows.add(row);
                } else if (selectedRowNames == null && row.isSelectedByDefault() && !row.isAlwaysSelected()) {
                    selectedRows.add(row);
                } else if (selectedRowNames != null && selectedRowNames.contains(row.getName())) {
                    selectedRows.add(row);
                }
            }
        }
        // preserve selected rows order
        Collections.sort(selectedRows, new LayoutRowsSorter(selectedRowNames));
        return selectedRows;
    }

    public static List<LayoutRow> getNotSelectedRows(Layout layout, List<String> selectedRowNames) {
        LayoutRow[] rows = layout.getRows();
        List<LayoutRow> notSelectedRows = new ArrayList<LayoutRow>();
        if (rows != null) {
            for (LayoutRow row : rows) {
                if (selectedRowNames == null && !row.isSelectedByDefault() && !row.isAlwaysSelected()) {
                    notSelectedRows.add(row);
                } else if (selectedRowNames != null && !row.isAlwaysSelected()
                        && !selectedRowNames.contains(row.getName())) {
                    notSelectedRows.add(row);
                }
            }
        }
        return notSelectedRows;
    }

    public static List<String> getDefaultSelectedRowNames(Layout layout, boolean showAlwaysSelected) {
        List<LayoutRow> selectedRows = getSelectedRows(layout, null, showAlwaysSelected);
        List<String> selectedRowNames = null;
        if (selectedRows != null && !selectedRows.isEmpty()) {
            selectedRowNames = new ArrayList<String>();
            for (LayoutRow row : selectedRows) {
                selectedRowNames.add(row.getName());
            }
        }
        return selectedRowNames;
    }

    /**
     * Returns an identifier computed from this definition so that an identical definition will have the same id.
     *
     * @since 5.5
     */
    public static String computeLayoutDefinitionId(LayoutDefinition layoutDef) {
        StringBuilder builder = new StringBuilder();
        builder.append(layoutDef.getName()).append(";");
        Map<String, String> templates = layoutDef.getTemplates();
        if (templates != null) {
            builder.append(templates.toString());
        }
        builder.append(";");
        LayoutRowDefinition[] rows = layoutDef.getRows();
        if (rows != null) {
            for (LayoutRowDefinition row : rows) {
                if (row != null) {
                    builder.append(computeLayoutRowDefinitionId(row)).append(",");
                }
            }
        }
        builder.append(";");
        Map<String, Map<String, Serializable>> properties = layoutDef.getProperties();
        if (properties != null) {
            builder.append(properties.toString());
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();
    }

    /**
     * Returns an identifier computed from this definition so that an identical definition will have the same id.
     *
     * @since 5.5
     */
    public static String computeLayoutRowDefinitionId(LayoutRowDefinition layoutRowDef) {
        StringBuffer builder = new StringBuffer();
        builder.append(layoutRowDef.getName()).append(";");
        builder.append(layoutRowDef.isSelectedByDefault()).append(";");
        builder.append(layoutRowDef.isAlwaysSelected()).append(";");
        WidgetReference[] widgets = layoutRowDef.getWidgetReferences();
        if (widgets != null) {
            for (WidgetReference widget : widgets) {
                if (widget != null) {
                    builder.append(widget.getName() + "(" + widget.getCategory() + ")").append(",");
                }
            }
        }
        builder.append(";");

        Map<String, Map<String, Serializable>> properties = layoutRowDef.getProperties();
        if (properties != null) {
            builder.append(properties.toString());
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();

    }

    /**
     * Returns an identifier computed from this definition so that an identical definition will have the same id.
     *
     * @since 5.5
     */
    public static String computeWidgetDefinitionId(WidgetDefinition widgetDef) {
        StringBuffer builder = new StringBuffer();
        builder.append(widgetDef.getName()).append(";");
        builder.append(widgetDef.getType()).append(";");
        builder.append(widgetDef.getTypeCategory()).append(";");

        FieldDefinition[] fieldDefinitions = widgetDef.getFieldDefinitions();
        if (fieldDefinitions != null) {
            for (FieldDefinition fieldDef : fieldDefinitions) {
                builder.append(fieldDef.getPropertyName() + ",");
            }
        }
        builder.append(";");

        Map<String, String> labels = widgetDef.getLabels();
        if (labels != null) {
            builder.append(labels.toString());
        }
        builder.append(";");
        Map<String, String> helpLabels = widgetDef.getHelpLabels();
        if (helpLabels != null) {
            builder.append(helpLabels.toString());
        }
        builder.append(";");

        WidgetDefinition[] subWidgets = widgetDef.getSubWidgetDefinitions();
        if (subWidgets != null) {
            for (WidgetDefinition widget : subWidgets) {
                if (widget != null) {
                    builder.append(computeWidgetDefinitionId(widget)).append(",");
                }
            }
        }
        builder.append(";");

        WidgetReference[] subWidgetRefs = widgetDef.getSubWidgetReferences();
        if (subWidgetRefs != null) {
            for (WidgetReference widget : subWidgetRefs) {
                if (widget != null) {
                    builder.append(widget.getName() + "(" + widget.getCategory() + ")").append(",");
                }
            }
        }
        builder.append(";");

        Map<String, Map<String, Serializable>> properties = widgetDef.getProperties();
        if (properties != null) {
            builder.append(properties.toString());
        }
        builder.append(";");
        Map<String, Map<String, Serializable>> widgetModeProperties = widgetDef.getWidgetModeProperties();
        if (widgetModeProperties != null) {
            builder.append(widgetModeProperties.toString());
        }
        builder.append(";");

        builder.append(widgetDef.isTranslated()).append(";");
        builder.append(widgetDef.isHandlingLabels()).append(";");

        Map<String, String> modes = widgetDef.getModes();
        if (modes != null) {
            builder.append(modes.toString());
        }
        builder.append(";");

        WidgetSelectOption[] selectOptions = widgetDef.getSelectOptions();
        if (selectOptions != null) {
            for (WidgetSelectOption option : selectOptions) {
                if (option != null) {
                    builder.append(option.getTagConfigId()).append(",");
                }
            }
        }
        builder.append(";");

        Map<String, Map<String, Serializable>> controls = widgetDef.getControls();
        if (controls != null) {
            builder.append(controls.toString());
        }
        builder.append(";");

        Integer intValue = new Integer(builder.toString().hashCode());
        return intValue.toString();
    }

    /**
     * Sorter that re-arranges rows according to the row names order.
     *
     * @since 5.6
     */
    public static class LayoutRowsSorter implements Comparator<LayoutRow> {

        protected List<String> orderedRowNames;

        private LayoutRowsSorter(List<String> orderedRowNames) {
            super();
            this.orderedRowNames = orderedRowNames;
        }

        @Override
        public int compare(LayoutRow o1, LayoutRow o2) {
            if (orderedRowNames == null || orderedRowNames.size() == 0) {
                return 0;
            }
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String id1 = o1.getName();
            String id2 = o2.getName();
            if (id1 == null && id2 == null) {
                return 0;
            }
            if (id1 == null) {
                return -1;
            }
            if (id2 == null) {
                return 1;
            }
            if (orderedRowNames.indexOf(id1) <= orderedRowNames.indexOf(id2)) {
                return -1;
            } else {
                return 1;
            }
        }

    }

}
