## API 接口文档
<#list groups as group>
### ${group.toMarkdownStr(group.groupName)}
<#list group.apiInfoList as apiInfo>
<span id="${apiInfo.anchor()}"></span>
#### [${apiInfo.methodName}](#${apiInfo.anchor()})
* **功能描述**: ${apiInfo.toMarkdownStr(apiInfo.apiComment)}
* **API PATH**: ${apiInfo.toMarkdownStr(apiInfo.path)}
* **HTTP Method**: ${apiInfo.httpMethod}

* **Query参数说明**:
    | 参数名称 | 参数类型 | 必填 | 默认值 | 描述 |
    |  ----  | ----  | ---- | ---- | ---- |
<#list apiInfo.params as param>
    | **${param.toMarkdownStr(param.name)}** | ${param.type} | ${param.required?string('yes', 'no')} | ${param.defaultValue} |${param.toMarkdownStr(param.desc!)} |
</#list>

<#if apiInfo.IsPostJsonApi() >
* Post Json Data 样例:

```
${apiInfo.postDataSample}
```
</#if>

<#if apiInfo.IsPostJsonApi() >
* Post Json 方式提交的 json 结构:
```
${apiInfo.postJsonSchemaDesc}
```
</#if>

<#if apiInfo.IsPostFormApi()>
* Post Form Fields:
    <#list apiInfo.PostFormFieldInfos() as param>
> * **${param.toMarkdownStr(param.name)}** : ${param.type}, ${param.toMarkdownStr(param.desc!)}
    </#list>
</#if>

* <a href="${apiInfo.TestPage()}" target="_blank">测试页面</a>:

* 返回结果的 JSON 结构:

```
${apiInfo.replySchemaDesc}
```

* 返回结果样例:

```
${apiInfo.replySampleData}
```
</#list>
</#list>
