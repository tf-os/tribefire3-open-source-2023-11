overrides
=========

Overrides are used to influence the environment with the feature runs. You can both set (or override) environment variables or system properties.

Using these overrides, you can use constructions such as these :
![fancy url](../images/settings.xml.variables.png)

In this case, all you need to is to define an override (especially a OverridingEnvironmentVariable)

<table>
  <tr>
    <td>name</td>
    <td><i>value</i></td>
  </tr>  
  <tr>
    <td>MYPROJECT_TRIBEFIRE_REPOSITORY_NAME</td>
    <td><i>your repository name</i></td>
  </tr>  
</table>

Basically, an override is simply a named value. It comes in two flavours

- OverridingEnvironmentVariable
- OverridingSystemProperty

instances of both of these types can be added to the configuration's respective property.

Notes
-----
- how to reference the overides in the supporting settings?  
Basically, you use the $ prefix and enclose your expression with curly braces. Depending on the tyoe of the override, you need to prefix the name of your override with env.
So, references to a environment variable <i>(OverridingEnvironmentVariable)</i> named <b>'myEnvVar'</b> should look like <b>${env.myEnvVar}</b>, and the system property <i>(OverridingSystemProperty)</i> named <b>'mySysProp'</b> should look like <b>${mySysProp}</b>.
