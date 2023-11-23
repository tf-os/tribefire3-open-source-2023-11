# Running an analysis via Jinni

In Jinni (provided the integration is active), you can call Zed as well. In its basic configuration, it will show you the extracted data and the ratings of the forensic modules in Zed. The rated problems are written into a file, see [forensics](../forensics/forensics.md).

The request is as follows:

```
Jinni zed <condensed name of terminal> -v[taciturn/terse/verbose/garrulous] -o<output directory, default ‘.’>
```


examples:

```
jinni zed -t my-group:my-artifact#1.0 -v terse -o C:\dev\a -w

jinni zed -t tribefire.ext.demo:my-model#1.0 -o C\dev\a
```

## request

name | alias | description 
------- | ----------- | -------
analyze-artifact | zed | run the standard analysis ZedRunnerContract

parameter | alias | description | default
------- | ----------- | ------- | -------
terminal | t | condensed qualified name of the artifact to analyze
outputDir | o | the directory to write the files to | . (current directory)
write | w| whether to write out also all assemblies, i.e. extraction, forensic data | false
verbosity | v | the detail-level of output, see [verbosity](../forensics/verbosity.md) | [verbose](../forensics/verbosity.md)


## response
property | type | description | output
------- | ----------- | ------- | -------        
fingerPrints | Resource | the ratings in expressive form ([see forensics](../forensics/forensics.md)) | always
extraction | Resource | the extraction data (as a generic assembly) in YAML format | always
dependencyForensics | Resource | the data of the dependency forensics (as a generic assembly) | if requested - *w*
classpathForensics | Resource | the data of the classpath forensics (as a generic assembly) | if requested - *w*
modelForensics | Resource | the data of the model forensics (as a generic assembly) | if requested - *w*
