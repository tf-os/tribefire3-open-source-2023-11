artifact identification
========================
An artifact identification comprises three values:

- the group id   
This is simply group as we are used to it.
- the artifact id    
This is again simply the name of the artifact.
- the version   
The version however is actually not a standard version, but rather a version-range. Depending of how the version is presented, it is interpreted differently.
  - if the version only specifies only the values for major and minor version parts, it will be automatically expanded into a range that allows for hotfix versions.
  - If you do however specify a hotfix by yourself, it will not be expanded, and what you entered will be taken as you entered it.
