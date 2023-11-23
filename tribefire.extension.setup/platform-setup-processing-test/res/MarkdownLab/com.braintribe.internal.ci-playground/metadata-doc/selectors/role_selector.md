---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Role Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The Role selector allows you to define which metadata should resolved depending a specific role."
sidebar: essentials
layout: page
permalink: role_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
Roles are created and assigned to users using the **Authentication and Authorization** access. Once you have created a set of users and roles, you can then use Role Selector to define the metadata's behavior.

## Example
To configure the role selector for <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>, you add a new instance of Role selector to your <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a>. Collapse the Role selector by clicking the collapse icon and then select the property `Roles`. Click **Add*** and enter a role which matches one of the roles defined in **Authentication and Authorization**. Click **Add** and **Finish**.

This means that this metadata is only resolved on users who have the assigned role.

{%include image.html file="metadata/RoleSelectorUsage07.png"%}

Otherwise, the metadata is not used.
