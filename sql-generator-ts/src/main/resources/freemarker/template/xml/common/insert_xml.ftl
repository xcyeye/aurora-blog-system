    <!-- ${methodDescription.INSERT.comment} -->
	<insert id="${methodDescription.INSERT.methodName}" parameterType="${mybatisXmlDefinition.parameterType}" useGeneratedKeys="true" keyProperty="uid">
        INSERT INTO au_${tableInfo.tableName} (
            <trim suffixOverrides=",">
                <#list mybatisXmlDefinition.columns as colm>
                    <#if colm??>
                <if test="${colm.testNotBlankExpression}">
                    ${colm.columnName},
                </if>
                    </#if>
                </#list>
            </trim>
        ) VALUES (
            <trim suffixOverrides=",">
            <#list mybatisXmlDefinition.columns as colm>
                <#if colm??>
                <if test="${colm.testNotBlankExpression}">
                 ${'#'}{${colm.javaColumnName}},
                </if>
                </#if>
            </#list>
            </trim>
        )
	</insert>
