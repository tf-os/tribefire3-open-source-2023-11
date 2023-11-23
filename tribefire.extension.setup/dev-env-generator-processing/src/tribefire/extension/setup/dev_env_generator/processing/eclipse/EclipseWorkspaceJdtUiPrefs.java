// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.setup.dev_env_generator.processing.eclipse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EclipseWorkspaceJdtUiPrefs extends EclipseWorkspaceHelper {

	public EclipseWorkspaceJdtUiPrefs(File devEnv) {

		super(devEnv, //
				".metadata/.plugins/org.eclipse.core.runtime/.settings", // folder name in eclipse-workspace
				"net.sf.eclipse.jdt.ui.prefs", // file name
				// content IF not patched but created new
				"""
				org.eclipse.jdt.core.classpathVariable.TOMCAT_HOME=@TOMCAT_HOME@
				cleanup.add_all=false
				cleanup.add_default_serial_version_id=false
				cleanup.add_generated_serial_version_id=true
				cleanup.add_missing_annotations=true
				cleanup.add_missing_deprecated_annotations=true
				cleanup.add_missing_methods=false
				cleanup.add_missing_nls_tags=false
				cleanup.add_missing_override_annotations=true
				cleanup.add_missing_override_annotations_interface_methods=true
				cleanup.add_serial_version_id=false
				cleanup.always_use_blocks=true
				cleanup.always_use_parentheses_in_expressions=true
				cleanup.always_use_this_for_non_static_field_access=false
				cleanup.always_use_this_for_non_static_method_access=false
				cleanup.array_with_curly=true
				cleanup.arrays_fill=false
				cleanup.bitwise_conditional_expression=false
				cleanup.boolean_literal=false
				cleanup.boolean_value_rather_than_comparison=true
				cleanup.break_loop=false
				cleanup.collection_cloning=false
				cleanup.comparing_on_criteria=false
				cleanup.comparison_statement=false
				cleanup.controlflow_merge=false
				cleanup.convert_functional_interfaces=false
				cleanup.convert_to_enhanced_for_loop=true
				cleanup.convert_to_enhanced_for_loop_if_loop_var_used=true
				cleanup.convert_to_switch_expressions=false
				cleanup.correct_indentation=true
				cleanup.do_while_rather_than_while=true
				cleanup.double_negation=false
				cleanup.else_if=false
				cleanup.embedded_if=false
				cleanup.evaluate_nullable=false
				cleanup.extract_increment=false
				cleanup.format_source_code=true
				cleanup.format_source_code_changes_only=false
				cleanup.hash=false
				cleanup.if_condition=false
				cleanup.insert_inferred_type_arguments=false
				cleanup.instanceof=false
				cleanup.instanceof_keyword=false
				cleanup.invert_equals=false
				cleanup.join=false
				cleanup.lazy_logical_operator=false
				cleanup.make_local_variable_final=true
				cleanup.make_parameters_final=false
				cleanup.make_private_fields_final=true
				cleanup.make_type_abstract_if_missing_method=false
				cleanup.make_variable_declarations_final=false
				cleanup.map_cloning=false
				cleanup.merge_conditional_blocks=false
				cleanup.multi_catch=false
				cleanup.never_use_blocks=false
				cleanup.never_use_parentheses_in_expressions=false
				cleanup.no_string_creation=false
				cleanup.no_super=false
				cleanup.number_suffix=false
				cleanup.objects_equals=false
				cleanup.one_if_rather_than_duplicate_blocks_that_fall_through=false
				cleanup.operand_factorization=false
				cleanup.organize_imports=true
				cleanup.overridden_assignment=false
				cleanup.overridden_assignment_move_decl=false
				cleanup.plain_replacement=false
				cleanup.precompile_regex=false
				cleanup.primitive_comparison=false
				cleanup.primitive_parsing=false
				cleanup.primitive_rather_than_wrapper=true
				cleanup.primitive_serialization=false
				cleanup.pull_out_if_from_if_else=false
				cleanup.pull_up_assignment=false
				cleanup.push_down_negation=false
				cleanup.qualify_static_field_accesses_with_declaring_class=false
				cleanup.qualify_static_member_accesses_through_instances_with_declaring_class=true
				cleanup.qualify_static_member_accesses_through_subtypes_with_declaring_class=false
				cleanup.qualify_static_member_accesses_with_declaring_class=true
				cleanup.qualify_static_method_accesses_with_declaring_class=false
				cleanup.reduce_indentation=false
				cleanup.redundant_comparator=false
				cleanup.redundant_falling_through_block_end=false
				cleanup.remove_private_constructors=true
				cleanup.remove_redundant_modifiers=false
				cleanup.remove_redundant_semicolons=true
				cleanup.remove_redundant_type_arguments=true
				cleanup.remove_trailing_whitespaces=true
				cleanup.remove_trailing_whitespaces_all=true
				cleanup.remove_trailing_whitespaces_ignore_empty=false
				cleanup.remove_unnecessary_array_creation=true
				cleanup.remove_unnecessary_casts=false
				cleanup.remove_unnecessary_nls_tags=true
				cleanup.remove_unused_imports=true
				cleanup.remove_unused_local_variables=false
				cleanup.remove_unused_method_parameters=false
				cleanup.remove_unused_private_fields=true
				cleanup.remove_unused_private_members=false
				cleanup.remove_unused_private_methods=true
				cleanup.remove_unused_private_types=true
				cleanup.return_expression=false
				cleanup.simplify_lambda_expression_and_method_ref=false
				cleanup.single_used_field=false
				cleanup.sort_members=false
				cleanup.sort_members_all=false
				cleanup.standard_comparison=false
				cleanup.static_inner_class=false
				cleanup.strictly_equal_or_different=false
				cleanup.stringbuffer_to_stringbuilder=false
				cleanup.stringbuilder=false
				cleanup.stringbuilder_for_local_vars=true
				cleanup.stringconcat_to_textblock=false
				cleanup.substring=true
				cleanup.switch=false
				cleanup.system_property=false
				cleanup.system_property_boolean=false
				cleanup.system_property_file_encoding=false
				cleanup.system_property_file_separator=false
				cleanup.system_property_line_separator=false
				cleanup.system_property_path_separator=false
				cleanup.ternary_operator=false
				cleanup.try_with_resource=false
				cleanup.unlooped_while=false
				cleanup.unreachable_block=false
				cleanup.use_anonymous_class_creation=false
				cleanup.use_autoboxing=false
				cleanup.use_blocks=true
				cleanup.use_blocks_only_for_return_and_throw=false
				cleanup.use_directly_map_method=false
				cleanup.use_lambda=true
				cleanup.use_parentheses_in_expressions=false
				cleanup.use_string_is_blank=false
				cleanup.use_this_for_non_static_field_access=false
				cleanup.use_this_for_non_static_field_access_only_if_necessary=true
				cleanup.use_this_for_non_static_method_access=false
				cleanup.use_this_for_non_static_method_access_only_if_necessary=true
				cleanup.use_type_arguments=false
				cleanup.use_unboxing=false
				cleanup.use_var=false
				cleanup.useless_continue=false
				cleanup.useless_return=false
				cleanup.valueof_rather_than_instantiation=false
				cleanup_profile=_Braintribe
				cleanup_settings_version=2
				content_assist_proposals_background=255,255,255
				content_assist_proposals_foreground=35,38,41
				eclipse.preferences.version=1
				formatter_profile=_Braintribe
				formatter_settings_version=22
				org.eclipse.jdt.ui.formatterprofiles.version=22
				
				spelling_locale_initialized=true
				typefilter_migrated_2=true
				useAnnotationsPrefPage=true
				useQuickDiffPrefPage=true				
				"""); 
	}
}
