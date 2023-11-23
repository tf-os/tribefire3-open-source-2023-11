<#-- The standard source folder -->
${static.createDir('src')}

<#-- The proposed common group folder, the user can change later, if he wishes -->
<#assign packageRoot='src/' + request.groupId?replace('.', '/')?replace('-', '_')>
${static.createDir(packageRoot)}
