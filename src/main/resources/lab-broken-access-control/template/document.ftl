<?xml version="1.0" encoding="utf-8"?>
<doc
        xmlns="http://javacoredoc.fugerit.org"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://javacoredoc.fugerit.org https://www.fugerit.org/data/java/doc/xsd/doc-2-1.xsd" >

    <#--
        This is a Venus Fugerit Doc (https://github.com/fugerit-org/fj-doc) FreeMarker Template XML (ftl[x]).
        For consideration of Venus Fugerit Doc and Apache FreeMarker integration see :
        https://venusdocs.fugerit.org/guide/#doc-freemarker-entry-point
        The result will be a :
    -->
    <!--
        This is a Venus Fugerit Doc (https://github.com/fugerit-org/fj-doc) XML Source Document.
        For documentation on how to write a valid Venus Doc XML Meta Model refer to :
        https://venusdocs.fugerit.org/guide/#doc-format-entry-point
    -->

    <#assign defaultTitle="Elenco persone di interesse">

    <metadata>
        <!-- Margin for document : left;right;top;bottom -->
        <info name="margins">10;10;10;30</info>
        <!-- documenta meta information -->
        <info name="doc-title">${docTitle!defaultTitle}</info>
        <info name="doc-subject">fj doc venus sample source FreeMarker Template XML - ftlx</info>
        <info name="doc-author">fugerit79</info>
        <info name="doc-language">en</info>
        <!-- property specific for xls/xlsx -->
        <info name="excel-table-id">data-table=print</info>
        <!-- property specific for csv -->
        <info name="csv-table-id">data-table</info>
        <footer-ext>
            <para align="right">${r"${currentPage}"} / ${r"${pageCount}"}</para>
        </footer-ext>
    </metadata>
    <body>
    <h head-level="1">${docTitle!defaultTitle}</h>
    <table columns="4" colwidths="25;25;25;25"  width="100" id="data-table" padding="2">
        <row header="true">
            <cell align="center"><para>Nome</para></cell>
            <cell align="center"><para>Cognome</para></cell>
            <cell align="center"><para>Titolo</para></cell>
            <cell align="center"><para>ID</para></cell>
        </row>
        <#if listPeople??>
            <#list listPeople as current>
                <row>
                    <cell><para>${current.name}</para></cell>
                    <cell><para>${current.surname}</para></cell>
                    <cell><para>${current.title}</para></cell>
                    <cell><para>${current.uuid}</para></cell>
                </row>
            </#list>
        </#if>
    </table>
    </body>

</doc>