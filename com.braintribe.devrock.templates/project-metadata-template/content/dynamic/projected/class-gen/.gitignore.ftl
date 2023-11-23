<#if !request.ide?? || request.ide != 'eclipse' || !request.builderOutputLibs?? || !request.builderOutputLibs?has_content>
	${template.ignore()}
</#if>
*
