# DSpace Ref+Rioxx by 4Science
This repository contains the integration addon for REF+RIOXX

The original work is explained at https://github.com/atmire/RIOXX/blob/master/README.md

Note that the general concepts are the same, 4Science simplified the installation procedure, essentially read the [New patch installation](#Patch-installation-procedures) and [How to disable REF](#Compatibility-with-the-REF-patch)

- [Introduction](#Introduction)
	- [Areas of DSpace affected by the RIOXX patch](#Areas-of-DSpace-affected)
	- [Areas of DSpace that have to be manually configured after applying the patch](#Areas-of-DSpace-manually-configured)
	- [Compatibility with the REF patch](#Compatibility-with-the-REF-patch)
- [Metadata mapping](#Metadata-mapping)
	- [General DSpace to RIOXXTERMS metadata mapping](#General-DSpace-RIOXXTERMS)
	- [RIOXX metadata derived from DSpace Bitstream metadata](#RIOXX-metadata-derived)
	- [dc:source mandatory where applicable](#dcsource-mandatory)
	- [dc:type fallback for rioxxterms:type](#dctype-fallback)
	- [fundref id for funders and orcid id for authors](#fundref-id)
	- [multiple funders and project](#multiple-funders)
	    - [configuration](#multiple-funders-configuration)
	    - [warning messages during submission](#multiple-funders-warning-messages)
	    - [Edit funder page](#edit-funders-page)
	- [license reference ali:license_ref](#license_ref)
	- [date completion](#date-completion)
	- [SWORD V2 configuration](#swordv2-configuration)
	    - [SWORD V2 mapping](#swordv2-mapping)
	    - [SWORD V2 Project/Funder ingestion](#swordv2-project-funder)
	    - [SWORD V2 Author attributes (ORCID and email)](#swordv2-author-attrib)
	    - [SWORD V2 Example Ingestion with Curl](#swordv2-curl)
- [Patch Installation Procedures](#Patch-installation-procedures)
	- [Prerequisites](#Prerequisites)	
	- [RIOXX Integration addon](#rioxx-integration-addon)		
		- [1. Run the pre-requisite Git command.](#Run-git-command)
		- [2. Rebuild and redeploy your repository](#Rebuild-redeploy)
		- [3. Restart your tomcat](#Restart-tomcat)
		- [4. Populate the RIOXX OAI-PMH end point](#Populate-RIOXX)
		- [5. Load Fundref authority data](#load-fundref-data)		
	- [Configure Submission forms or other metadata ingest mechanisms](#Configure-submission)
- [Verification](#Verification)
	- [RIOXX Metadata Registry](#RIOXX-metadata-registry)
	- [Submission forms based on template](#Submission-forms-template)
	- [OAI-PMH endpoint](#OAI-PMH-endpoint)
- [Troubleshooting](#Troubleshooting)	
	- [RIOXX test items are not visible in OAI-PMH endpoint](#RIOXX-test-OAI-PMH-endpoint)

# Introduction <a name="Introduction"></a> 


This documentation will help you deploy and configure the RIOXXv2 Application Profile for DSpace 5.10 and 6.3. The patch has been implemented in a generic way, using a maven artifact. This means that changes to your existing DSpace installation could be overridden by this procedure.

## Areas of DSpace affected by the RIOXX patch <a name="Areas-of-DSpace-affected"></a> 

Following areas of the DSpace codebase are affected by the RIOXX patch:  
- **Metadata Registries**: a new RIOXX metadata registry will be added with a number of new fields. This does not affect your existing metadata schema's or items  
- **OAI Endpoint**: a new RIOXX endpoint will become available in your OAI-PMH interface, in order to allow external harvesters to harvest your repository metadata in RIOXX compliant format.  
- **SWORD V2 Endpoint**: The SWORD V2 ingest will be improved to allow for RIOXX compliant SWORD V2 ingests into DSpace.

It is important to realize that your existing item metadata and item display pages will **NOT** be modified as part of the RIOXX patch.

## Areas of DSpace that have to be manually configured after applying the patch  <a name="Areas-of-DSpace-manually-configured"></a>

**Submission forms**: the configuration file that defines your submission forms, **input-forms.xml** needs to be be extended with a number of new entry options.

Because the vast majority of institutions make at least small tweaks to the submission forms, there is no opportunity to apply a patch to a standardized file. A template submission form file where the new REF and RIOXX fields are highlighted can be found on Github:

[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms.xml) 
[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission.xml) 

See rioxxterms.* fields + value pairs + dcterms.dateAccepted 

Please note that we have also provided examples for *REF only* or *RIOXX only* input forms and submission configuration: 
[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms-ref.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms-ref.xml)
[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission-ref.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission-ref.xml)

[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms-rioxx.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms-rioxx.xml)
[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission-rioxx.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission-rioxx.xml)

## Compatibility with the REF patch  <a name="Compatibility-with-the-REF-patch"></a>

The RIOXX patch contains also the REF patch. Both are installed at the same time. But you can choose to disable REF feature.

To disable the REF feature set the follow configuration to **false**:
1. [https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/modules/rioxx.cfg#L9](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/modules/rioxx.cfg#L9)
2. [https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/modules/item-compliance.cfg#L32](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/modules/item-compliance.cfg#L32)

Manually change this configuration:
1. From [https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms.xml) remove the metadata: refterms.panel and refterms.dateFirstOnline
2. From [https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/item-submission.xml) remove the step: REFExceptionStep and REFComplianceStep
3. If you enabled the XML Workflow please remove https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/workflow.xml#L38 and https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/workflow.xml#L56


# Metadata mapping <a name="Metadata-mapping"></a>

Before diving into the process of installing the RIOXX patch, it is crucial that you take note of the specific DSpace=>RIOXX metadata mapping that this patch implements. Your use of the different dc and dcterms fields in DSpace may be different from a standard installation, in which case you may need to do some additional activities before or after applying the patch.

The following table lists the different metadata elements, according to the order specified in [http://rioxx.net/v2-0-final/](http://rioxx.net/v2-0-final/).  
The DSpace metadata column indicates where the corresponding RIOXX elements are stored in the DSpace metadata.

Existing fields from the dc and dcterms namespace were used where possible. A number of new fields were added in a dedicated rioxxterms metadata registry.

## General DSpace to RIOXXTERMS metadata mapping <a name="General-DSpace-RIOXXTERMS"></a> 

|  DSpace metadata   |  RIOXX element    |   example DSpace value |  example RIOXX value    |    
| ------------------ | ----------------- | ---------------------- | ----------------------- |
| Bitstream metadata|ali:free_to_read | See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below.
| dc.rights.uri | ali:license_ref  |  [http://creativecommons.org/licenses/by/3.0/igo/](http://creativecommons.org/licenses/by/3.0/igo/)|  ` <ali:license_ref start_date="2015-01-20"> `  <br> ` http://creativecommons.org/licenses/by/3.0/igo/ ` <br> ` </ali:license_ref> ` |
| dc.date.issued| ali:license_ref:startdate | 2015-01-20| ` <ali:license_ref start_date="2015-01-20"> ` <br> `  http://creativecommons.org/licenses/by/3.0/igo/ `<br> `</ali:license_ref> `|
| dc.coverage| dc:coverage| Columbus, Ohio, USA; Lat: 39 57 N Long: 082 59 W| ` <dc:coverage> ` <br>  ` Columbus, Ohio, USA; Lat: 39 57 N Long: 082 59 W ` <br> ` </dc:coverage> `|
| dc.description.abstract| dc:description| example item | ` <dc:description> `<br> `example item`<br>`</dc:description>`|
| Bitstream metadata| dc:format| See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below. |
| Bitstream metadata| dc:identifier| See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below. |
| rioxxterms.openaccess.uri| dc:identifier| See separate table with bitstream derived metadata below. | See separate table with bitstream derived metadata below. |
| dc.language.iso| dc:language | en-GB|`<dc:language> ` <br> ` en-GB ` <br> `  </dc:language>` |
| dc.publisher| dc:publisher | PLOS ONE|` <dc:publisher> `<br> `PLOS ONE`<br>`</dc:publisher>`|
| dc.relation.uri| dc:relation |[http://datadryad.org/resource/doi:10.5061/dryad.tg469](http://datadryad.org/resource/doi:10.5061/dryad.tg469) |`<dc:relation>`<br>`http://datadryad.org/resource/doi:10.5061/dryad.tg469`<br>`</dc:relation>`|
| dc.identifier.isbn| dc:source | 0-14-020652-3|`<dc:source>`<br>`0-14-020652-3`<br>`</dc:source>`| 
| dc.identifier.issn| dc:source| 1456-2979 |`<dc:source>`<br>`1456-2979`<br>`</dc:source>`| 
| dc.subject| dc:subject | example |`<dc:subject>`<br>`example`<br>`</dc:subject>`|
| dc.title| dc:title| Title:Subtitle|`<dc:title>`<br>`Title:Subtitle`<br>`</dc:title>` |
| dcterms.dateAccepted| dcterms:dateAccepted| 2015-02-10|`<dcterms:dateAccepted>`<br>`2015-02-10`<br>`</dcterms:dateAccepted>`|
| rioxxterms.apc| rioxxterms:apc| paid|`<rioxxterms:apc>`<br>`paid`<br>`</rioxxterms:apc>`|
| dc.contributor.author (first)| rioxxterms:author (+ attribute "first-named-author=true")|Lawson, Gerry|`<rioxxterms:author id="http://orcid.org/0000-0002-1395-3092" first-named-author="true">`<br>`Lawson, Gerry`<br>`</rioxxterms:author>`|
| dc.contributor.author (others)| rioxxterms:author| Lawson, Gerry|`<rioxxterms:author id="http://orcid.org/0000-0002-1395-3092" first-named-author="false">`<br>`Lawson, Gerry`<br>`</rioxxterms:author>`|
| dc.contributor.* (non authors)| rioxxterms:contributor| Lawson, Gerry|`<rioxxterms:contributor id="http://orcid.org/0000-0002-1395-3092">`<br>`Lawson, Gerry`<br>`</rioxxterms:contributor>`|
| rioxxterms.identifier.project| rioxxterms:project| EP/K023195/1|`<rioxxterms:project rioxxterms:funder_name="Engineering and Physical Sciences Research Council" rioxxterms:funder_id="http://dx.doi.org/10.13039/501100000266">`<br>`EP/K023195/1`<br>` </rioxxterms:project>`|
| rioxxterms.funder| rioxxterms:project| Engineering and Physical Sciences Research Council|`<rioxxterms:project rioxxterms:funder_name="Engineering and Physical Sciences Research Council" rioxxterms:funder_id="http://dx.doi.org/10.13039/501100000266">`<br>`EP/K023195/1`<br>` </rioxxterms:project>`|
| rioxxterms.funder.project| rioxxterms:project| d90b33fb16bbac756120dd85cbad3940 <br><sub>(NOTE: This is an internal key identifying the project, it will be hidden by default for non-admin users)</sub> |`<rioxxterms:project rioxxterms:funder_name="Engineering and Physical Sciences Research Council" rioxxterms:funder_id="http://dx.doi.org/10.13039/501100000266">`<br>`EP/K023195/1`<br>` </rioxxterms:project>`|
| dc.date.issued| rioxxterms:publication_date| 2015-02-15 |`<rioxxterms:publication_date>`<br>`2015-02-15`<br>` </rioxxterms:publication_date>`|
| rioxxterms.type with dc.type fallback| rioxxterms:type| Book|`<rioxxterms:type>`<br>`Book`<br> `</rioxxterms:type>`|  
| rioxxterms.version| rioxxterms:version| AO|`<rioxxterms:version>`<br>`AO`<br> `</rioxxterms:version>`|
| rioxxterms.versionofrecord| rioxxterms:version_of_record|[http://dx.doi.org/10.1006/jmbi.1995.0238](http://dx.doi.org/10.1006/jmbi.1995.0238)|` <rioxxterms:version_of_record>`<br>`http://dx.doi.org/10.1006/jmbi.1995.0238`<br>`</rioxxterms:version_of_record>`|

## RIOXX metadata derived from DSpace Bitstream metadata <a name="RIOXX-metadata-derived"></a>

Because DSpace supports multiple files per attached metadata record, there is a split between information stored in the metadata record and information stored with the bitstreams.  

For the three fields shown in the table below, data is retrieved from the bitstream metadata for the bitstream indicated as the "primary bitstream" ("embargo" information is currently retrieved on all bitstreams of the ORIGINAL bundle). Logic for selection of the primary bitstream has been improved by adding the option of identifying it by matching the filename to a regex pattern  (check https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/modules/oai.cfg#L120). By default if the user manually selected the bitstream as primary then the RIOXX OAI context will honour that, otherwise the bitstream filenames will be compared to the regex and if a match is found that will be designated the primary; if there is no regex match then primary bitstream defaults to the first in the list of the uploaded files.

| DSpace bitstream | RIOXX element | example DSpace value| example RIOXX value|
|----------------|----------------|----------------------|---------------------|
| format| dc:format| application/pdf|` <dc:format> ` <br> `application/pdf` <br>  `</dc:format>`|
| url| dc:identifier|[https://example.com/dspace/bitstream/123456789/10/1/example.pdf](https://example.com/dspace/bitstream/123456789/10/1/example.pdf)|` <dc:identifier> ` <br> ` https://example.com/dspace/bitstream/123456789/10/1/example.pdf ` <br> ` </dc:identifier>` |
| embargo| ali:free_to_read| 2015-08-27|` <ali:free_to_read start_date="2015-08-27">` <br> ` </ali:free_to_read>` |

The RIOXX patch relies on the activation of the standard DSpace embargo functionality, and will ready the date for ali:free_to_read from the Resource policy set on the bitstream.  
Currently, there is no specific support provided for end_date, assuming that once access is open, there is no specific use case for closing it again.

## dc:source mandatory where applicable <a name="dcsource-mandatory"></a>

The RIOXX specification states that dc.source is mandatory where applicable. The DSpace RIOXX patch does currently not enforce this: ISSN and ISBN are merely provided in the crosswalk when they are filled out.  
In the standard DSpace submission form, ISBN and ISSN can be provided in a field for identifiers, that has a dropdown where the user first needs to select the identifier type.

If you are primarily collecting materials for which an ISSN applies, it is recommended to use a separate, custom field for ISSN that fills dc.identifier.issn, and make that field mandatory.

## dc:type fallback for rioxxterms:type <a name="dctype-fallback"></a>

There is a substantial overlap between the vocabulary for rioxxterms:type and the standard list for dc.type. To ensure all of the rioxxterms types are available to your submitters, it is recommended to put a specific rioxxterms.type field in place, that uses the specifc vocabulary.

However, in case rioxxterms.type is absent in your items, the OAI-PMH crosswalk provides a basic mapping between dc.type and rioxxterms:type for those types that can be unambiguously mapped:

|DSpace type | RIOXX type
-------------|------------
| Article| Journal Article/Review
| Book| Book
| Book chapter| Book chapter
| Technical Report| Technical Report
| Thesis| Thesis
| Working Paper| Working paper

## fundref id for funders and orcid id for authors <a name="fundref-id"></a> 

Fundref DOI's for funders and ORCID id's for authors are NOT stored in the actual metadata value for the fields above. The metadata values only contain the string representations of funders and authors.  
The RIOXX OAI-PMH crosswalks retrieves the ORCIDs for authors and fundref ids for funders from the DSpace SOLR Authority cache. This feature was added in DSpace 5, but was backported to DSpace 3.x and 4.x as part of the RIOXX patch.

Right now, this only affects institutions that use the XMLUI, since the JSPUI has no web UI yet for working with this authority cache. However, JSPUI institutions are still compliant with RIOXX as the string representations of funder and author are included in the RIOXX OAI-PMH crosswalk.

## multiple funders and project <a name="multiple-funders"></a> 

The item submission has been updated with a new step called projects. This step allows the submitter to associate his submission with one or more projects. Each of these projects is associated with a funder. 

Using the 'Lookup Project' button the submitter can lookup projects that are already associated with another submission. When a project is selected, the associated funder will be automatically filled out as well.

if a project was not entered before, the submitter can create a new project. The new project's identifier must be filled out in the project input field and a funder to associate with the new project must be selected by using the 'Lookup Funder' button. 

It is not possible to create a new funder during the submission, only existing funders can be selected. Refer to section [5. Load Fundref authority data](#load-fundref-data) in the Patch Installation procedures to learn how to load funder data into DSpace.

### configuration <a name="multiple-funders-configuration"></a> 

The behaviour of this new submission step can be configured in *dspace/config/modules/rioxx.cfg*. 

Property submission.funder.required is used to configure if at least one project funder pair should be filled in before continuing to the next submission step.
 
```
submission.funder.required = true
```
 
Property submission.funder.enableDcSponsorship is used to enable the addition of sponsorships or other sources of funding that do not provide a formal project or grant ID. 
If this property is enabled a free text field will be available in the project step. This free text field is not authority controlled.
 
It is also possible to configure a default project funder pair to be used when the submitter did not select any project funder pairs before finishing the projects step.
Properties authority.default.funder, authority.default.funderID and authority.default.project must all three be filled in for the default project funder pair to be added automatically.  

```
authority.default.funder = Default funder
authority.default.funderID = 10.99999/999999999
authority.default.project = Default project
```

authority.default.funder is the name of the default funder.
authority.default.funderID is the ID of the default funder.
authority.default.project is the name of the default project.

### warning messages during submission <a name="multiple-funders-warning-messages"></a>

As described in the [multiple funders and projects configuration ](#configuration-) there are different combinations that state if a funder is required, and what values to use as a default.
Depending on what combination is configured, a specified warning message will be shown.

There following rules are currently in place to set these warning messages:

|Combination|Warning message|
|----|----|
|Project and funder combination is required, and a default funder/project is configured|Caution: Without manually selecting a project or funder, this submission will receive project ID "{0}" and funder "{1}".  **NOTE:{0} and {1} are the project and funder configured in dspace/config/modules/rioxx.cfg**
|Project and funder combination is required, No defaults configured|Caution: Without manually selecting a project or funder, this submission will not receive the required  project ID and funder. Please make sure to complete these fields using the provided lookup.
|Project and funder combination not required, No defaults configured|Caution: Without manually selecting a project or funder, this submission will not receive a project ID or funder. If this submission is desired to be RIOXX compliant, please make sure to complete these fields using the provided lookup.

### Edit funders page <a name="edit-funders-page"></a> 

An additional functionality to edit the funding of an already archived item has been added. This enables users with the proper rights to add or remove project and funder pairs from the item.

This can be accessed on the "Edit item" page present in the user's context. This contains a new "Item Funding" tab that encompasses the addition and removal of project/funder.
This does not however contain the assumption that a default funder and project should be used when no project/funder pair is given. It is the end-users responsibility to ensure the integrity of the item's metadata.

The rest of this new page is used in the same way as the normal "ProjectStep" during submission.
A user can select a project using the provided lookup button, which will also autocomplete the appropriate funder.
If a user wants to enter a new project, he/she can enter one manually and add a funder using the lookup. (Empty values are prohibited during this addition as the default project/funder is disabled)

## license reference ali:license_ref<a name="license_ref"></a> 

The input forms customisations provide an input field to specify the license reference that is exposed by RIOXX. This input field uses metadata field rioxxterms.licenseref.uri to store the license reference. 

The Creative Commons license submission step has been enabled to provide a fallback for the custom rioxxterms.licenseref.uri field. The license selected in this step is stored in metadata field dc.rights.uri.

If a DSpace item does not have a rioxxterms.licenseref.uri value, the dc.rights.uri value is used as fallback.

A DSpace item will not be available in RIOXX if both metadata fields rioxxterms.licenseref.uri and dc.rights.uri are empty. 

## date completion <a name="date-completion"></a> 

The RIOXX specification requires dates to be in format YYYY-MM-DD. When a DSpace metadata field contains a shorter date in format YYYY-MM or YYYY, the RIOXX crosswalks will complete the date into the full format required by RIOXX.

Examples:

- dc.date.issued "2015" in DSpace becomes "2015-01-01" when it is exposed in RIOXX as ali:license_ref:start_date.
- dcterms.dateAccepted "2014-05" in DSpace becomes "2014-05-01" when it exposed in RIOXX as dcterms:dateAccepted 

## SWORD V2 configuration <a name="swordv2-configuration"></a> 

The DSpace SWORD V2 interface is designed to work optimally with a specially designed XML schema allowing for unambiguous transmission of information such as licensing and funding.

An example XML input file can be found on https://github.com/jisc-services/Public-Documentation/blob/master/PublicationsRouter/sword-out/DSpace-RIOXX-XML.md.

The configuration for the RIOXX SWORD V2 mapping can be found in *dspace/config/modules/swordv2-server.cfg*. 

The RIOXX metadata mapping configuration in this file can be recognized by the 'simplerioxx' prefix. This prefix is a reference to the Simple RIOXX ingester which is added to DSpace by the RIOXX patch to allow RIOXX compliant SWORD V2 ingests.

### SWORD V2 RIOXX Mapping overview <a name="swordv2-mapping"></a>
```
simplerioxx.dcterms.description = dc.description
simplerioxx.dcterms.publisher = dc.publisher
simplerioxx.dcterms.title = dc.title
simplerioxx.rioxxterms.type = rioxxterms.type
simplerioxx.dcterms.language = dc.language.iso
simplerioxx.dcterms.abstract = dc.description.abstract
simplerioxx.rioxxterms.version_of_record = rioxxterms.versionofrecord, dc.identifier.doi
simplerioxx.dcterms.subject = dc.subject
simplerioxx.dcterms.dateAccepted = dcterms.dateAccepted
simplerioxx.rioxxterms.publication_date = dc.date.issued
simplerioxx.pubr.author = dc.contributor.author
simplerioxx.pubr.contributor = dc.contributor
simplerioxx.ali.license_ref = dc.rights.uri
simplerioxx.dcterms.rights = dc.rights
simplerioxx.pubr.embargo_date = dc.rights.embargodate
simplerioxx.rioxxterms.project = workflow.newfunderprojectpair
simplerioxx.rioxxterms.version = rioxxterms.version
simplerioxx.pubr.sponsorship = dc.description.sponsorship
simplerioxx.pubr.openaccess_uri = rioxxterms.openaccess.uri
```
Please note that if you are already using the simpledc mapping from the same configuration file for your SWORD deposit, they will still be considered unless they conflict with the simplerioxx mappings (if the same MD field is involved in both a simpledc mapping and a simplerioxx mapping, the simplerioxx mapping will have priority).

### SWORD V2 Project/Funder ingestion <a name="swordv2-project-funder"></a>

The XML schema allows for project/funder information to be supplied in two XML elements:
```
<rioxxterms:project funder_name="Some Funder Name" funder_id="Identifier-URL">Project/Grant-Number</rioxxterms:project>
<pubr:sponsorship>Funder: Some Funder Name, Grant no: Project/Grant-Number, Funder ID:  Identifier-URL </pubr:sponsorship>
```

<p><b>rioxxterms:project</b> 
<br> 
The RIOXX patch will attempt to match the rioxxterms:project details against funders in the fundref-registry (see [5. Load Fundref authority data](#load-fundref-data)) first on funder_id and, as a fallback, on funder_name. If a match is found, the DSpace metadata fields rioxxterms.identifier.project, rioxxterms.funder and rioxxterms.funder.project will be filled with respectively the Project/grant-number, the Funder name, and the internal key registered in DSpace for this project. If no match can be found, the metadata field rioxxterms.newfunderprojectpair will be filled with the full details, with the intention that these are curated by a repository manager/reviewer and funder details manually added to the registry.</p> 
<p><b>rioxxterms.newfunderprojectpair</b> 
<br> 
This metadata field is created when no funder is found in the DSpace Funder Registry that matches that supplied via SWORD in the rioxxterms:project XML element. The value of this field is filled in with the information received via SWORD in this format: 
</p> 
<pre>funder-ID::funder-name::project-code</pre> 
<p>Note that it is possible to receive a project-code without any funder details. In that case this would be presented as: 
</p> 
<pre>null::null::project-code</pre> 

<p><b> pubr:sponsorship</b> 
<br> 
The contents of the pubr:sponsorship element are always added to the Dspace metadata field dc.description.sponsorship as a textual description of the funding (without modification by the ingester) and will never be exposed on the RIOXX OAI endpoint. 
</p> 

### SWORD V2 Author attributes <a name="swordv2-author-attrib"></a>

The XML schema allows for additional author attributes to be supplied in the XML <contributor> and <author> elements : 

```
<pubr:contributor id="http://orcid.org/0000-0002-8257-7777" email="johnsmith@yahoo.com">Smith, John </pubr:contributor>
<pubr:author id="http://orcid.org/0000-0002-8257-4088" email="teva@yahoo.com">Vernoux, Teva </pubr:author>
```
	
Please note that these attributes will not be stored as metadata in the ingested item itself but, if the corresponding dspace fields (see [SWORD V2 Rioxx mappings](#swordv2-mapping)) are defined in your repository in the authority core, they will be stored as attributes of the author record within the SOLR core (which means that these attributes are available when using the author lookup in a manual submission for example).

NOTE: to select "first-named-author=true" on dc.contributor.author the user can add attribute corresp="true" to the ingested xml sword file.
```
<pubr:contributor id="http://orcid.org/0000-0002-8257-7777" email="johnsmith@yahoo.com">Smith, John </pubr:contributor>
<pubr:author id="http://orcid.org/0000-0002-8257-4088" email="teva@yahoo.com" corresp="true">Vernoux, Teva </pubr:author>
```

### SWORD V2 Example Ingestion with Curl <a name="swordv2-curl"></a>


<b>Step 1 : Ingest metadata</b>
```
curl -v -i <*your DSpace repository*>/swordv2/collection/<*collection in which the item should be ingested*> --data-binary "@<*your xml MD file*>" -H "Content-Type: application/atom+xml" -H "In-Progress: true" --user "<*e-mail address submitter*>"
```

<b>Step 2 : Ingest bitstreams</b>
```
curl -v -i <*your DSpace repository*>/edit-media/3 --data-binary "@<*zip file containing the bitstreams*>" -H "Content-Type: application/zip" -H "Packaging: http://purl.org/net/sword/package/SimpleZip" -H "Content-Disposition:filename=<*zip file containing the bitstreams*>"  --user "<*e-mail address submitter*>"
```

<b>Step 3 : Finish submission</b>
```
curl -X POST -v -i <*your DSpace repository*>/edit/3 -H "In-Progress: false" -H "content-length: 0" --user "<*e-mail address submitter*>"
```


# Patch Installation Procedures <a name="Patch-installation-procedures"></a>

## Prerequisites  <a name="Prerequisites"></a> 

A new simplified addon has been released by 4Science built as a Maven Module. The artifacts for DSpace 5.10 and DSpace 6.3 have been released as public on <a href="https://nexus.4science.it/">4Science Nexus repository</a>

**__Important note__**: if you use DSpace 5.10 or DSpace 6.3 default versions you have to IMMEDIATELLY UPGRADE to the last line of development (5.11-SNAPSHOT or 6.4-SNAPSHOT). This is required because these versions may have some malfunctions due to dependencies (e.g. upgrade for Bower, JRuby, SASS dependency) or changed third party policies (e.g. GeoLite database feature for geolocation points). 
* to upgrade your DSpace from 5.10 to 5.11-SNAPSHOT use https://github.com/4Science/rioxxintegration/releases/download/5.10.0/jisc-from-5_10-to-5_11-patch.diff
* to upgrade your DSpace from 6.3 to 6.4-SNAPSHOT (current last commit 5c5e415276e11bfafaabb51819b38278862c2e91) use https://github.com/4Science/rioxxintegration/releases/download/6.4.0-beta/jisc-from-6_3-to-5c5e415276e11bfafaabb51819b38278862c2e91-patch.diff

To be able to install the patch, you will need the following prerequisites:

* A running DSpace 5.10 or 6.3 instance
* Git should be installed on the machine to apply the prerequisite patch.

## RIOXX Integration addon <a name="rioxx-integration-addon"></a>

After upgrading your DSpace at 5.11-SNAPSHOT or 6.4-SNAPSHOT (currently the released date of stable versions is not yet known) you can install the patch to download the RIOXX Integration during the default DSpace build procedure. The patch upgrades the Maven POM files to retrieve and install the RIOXX code customizations.

* dependencies for 5.x: https://github.com/4Science/rioxxintegration/releases/download/5.11.0-beta/jisc-5_11-SNAP-patch.diff
* dependencies for 6.x: https://github.com/4Science/rioxxintegration/releases/download/6.4.0-beta/jisc-6_4-SNAP-patch.diff


### 1. Run the pre-requisite Git command. <a name="run-git-command"></a>

Run the following command where `<patch file>` needs to be replaced with the name of the patch:

``` 
git apply --check <patch file>
```

This command will return whether it is possible to apply the patch to your installation. This should pose no problems where DSpace is not customized or where not many customizations are present. 

If the check is successful, the patch can be installed without any problems. Otherwise, you will have to merge some changes manually.

To apply the patch, the following command should be run where `<patch file>` is replaced with the name of the patch file. 

``` 
git apply --whitespace=nowarn --reject <patch file>
```

This command will tell git to apply the patch and ignore unharmful whitespace issues. The `--reject` flag instructs the command to continue when conflicts are encountered and saves the problematic code chunks to a `.rej` file so you can review and apply them manually later on. Before continuing to the next step, you have to resolve all merge conflicts indicated by the `.rej` files. After solving the merge conflicts, remove all the `.rej` files.


For example to install the 5.x RIOXX Integration plugin on top of a 5.10 DSpace version you have:

```
git checkout dspace-5.10
git apply --whitespace=nowarn --reject <jisc-from-5_10-to-5_11-patch.diff>
git apply --whitespace=nowarn --reject <jisc-5_11-SNAP-patch.diff>
```

### 2. Rebuild and redeploy your repository <a name="Rebuild-redeploy"></a>

After the patch has been applied, the repository will need to be rebuilt.   
DSpace repositories are typically built using the Maven and deployed using Ant. 

**__Important note__**: RIOXX and REF Integration are fully supported only with XMLUI Mirage2 and XML Workflow

To build application please use:

```
mvn clean -U package -Dmirage2.on=true
```

The new RIOXX integration will be downloaded automatically from 4Science Nexus Repository and installed by the build procedure (Maven/Ant)

The new metadata fields needed by RIOXX integration are automatically installed during the startup of the application thanks to the Flyway utility.

If you are not seeing the fields in your registry, you can import the rioxx fields manually by executing:

```
dspace/bin/dspace dsrun org.dspace.administer.MetadataImporter -f <dspace.dir>/config/registries/rioxxterms-types.xml -u
```

### 3. Restart your tomcat <a name="Restart-tomcat"></a>

After the repository has been rebuilt and redeployed, the tomcat will need to be restarted to bring the changes to production. 

### 4. Populate the RIOXX OAI-PMH end point <a name="Populate-RIOXX"></a>
 
To Populate the RIOXX end point, used for harvesting, run the following command: 

```
[dspace]/bin/dspace oai import -c
```

This will Populate the RIOXX OAI endpoint that will be available on 

```
<server-url>/oai/rioxx?verb=ListRecords&metadataPrefix=rioxx
```

If you want to avoid multiple manual executions of this script during testing, you can always add it to your scheduled tasks (crontab), and have it execute every hour or every 15 minutes.  

Do note that the more items your repository contains, the more resource intensive this task is. Be careful scheduling this task frequently on production systems! On production systems we still highly recommend a daily frequency.


### 5. Load Fundref authority data <a name="load-fundref-data"></a>

From DSpace 5 there is a new SOLR based infrastructure for authority control, originally used for storing authority data from ORCID. For RIOXX, this infrastructure was used to hold Fundref authority data.  
Even though the SOLR core with authority data can be enabled for JSPUI, there is no support yet for lookup in this registry through the submission forms in JSPUI.

As the source, DSpace relies on the RDF file published by Crossref at:  
[http://dx.doi.org/10.13039/fundref_registry](http://dx.doi.org/10.13039/fundref_registry)

More information about this file is available at:  
[http://help.crossref.org/fundref-registry](http://help.crossref.org/fundref-registry)

Download this file to your DSpace server.

**__Important note__**: since the file is large, you may need to give more memory to your "dspace" script - you can open in edit the "dspace" file and find the "-Xmx" parameter. Set it up to give 2 Gigabytes of memory "-Xmx2G".

The "PopulateFunderAuthorityFromXML" script will add new funders as authorities for inclusion in rioxxterms.project, where funder and project id are exposed.  
If you are executing the script for the first time, your SOLR authority cache will be loaded with all funders present in the fundref export.  
After that, you can use the same script when there is a new release of the fundref export. In this case, both new funders will be added and information from previously added funders will be updated.

To run the script:

```
./dspace dsrun org.dspace.scripts.PopulateFunderAuthorityFromXML -f {funder-authority-rdf}
```

arguments:  
-f: The RDF XML file containing the funder authorities  
-t: Test if the script works correctly. No changes will be applied.

Note: Using the above PopulateFunderAuthorityFromXML script is the only way to create funders in DSpace. If an item is ingested into DSpace, for example by using SWORD V2, and this item contains a funder project pair with a funder that does not yet exists in DSpace, then DSpace will not attempt to create this funder but will instead store the project funder pair in metadata field workflow.newfunderprojectpair. 

## Configure Submission forms or other metadata ingest mechanisms <a name="Configure-submission"></a>

Now that the new fields are present in your metadata schema's, you have to ensure that these fields can be filled. If your institution is relying on manual entry using the DSpace submission forms, you can go over the template input-forms.xml file on Github to see how the different new RIOXX fields can be included. If you are relying on automated ingests using SWORD or integrations with your CRIS system, you will likely need to customize the mapping and integration with those systems. This is beyond the scope of the patch and this documentation.

Note that simply adding the new RIOXX fields to the existing DSpace fields may create confusion for your end users. For example, the DSpace default "sponsor" field is similar to the RIOXX specific project and funder linking. Likewise, the "File Description" field that DSpace offers in the file upload dialog, has a similar purpose than the RIOXX "version" field. It is recommended to go over your submission forms entirely to verify that it is clear for your end users which fields are used for which purpose. Possibly, you may want to remove or repurpose existing DSpace default fields.

# Verification <a name="Verification"></a>

## RIOXX Metadata Registry <a name="RIOXX-metadata-registry"></a>

As an administrator, navigate to the standard DSpace administrator page "Registries >> Metadata".  
On this page, you should be able to see the new RIOXX metadata schema. When clicking on the link, you should see the different fields in the metadata schema. This new registry shouldn't be empty.

## Submission forms based on template <a name="Submission-forms-template"></a>

This verification assumes that you have modified your input-forms.xml based on:

[https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms.xml](https://github.com/4Science/rioxxintegration/blob/master/rioxxintegration-api/src/main/resources/dspace/config/input-forms.xml)

Start the submission of a new item in a DSpace collection that uses our custom submission form config.  
After the collection selection, a custom step is included to support adding multiple funders and project IDs. In this step you should be able to add this field:

* rioxxterms:project: Funder lookup and project field

in the first screen of the next step, you should be able to find following new fields:

* ali:license_ref: license URI and License start date    
     *   The RIOXX spec supports the provision of multiple license ref's and dates. In DSpace, we are currently only supporting a single license URL and a single date. If multiple usage licenses apply, it is recommended to pick the most open one.   
* dcterms.dateAccepted
* rioxxterms:version
* rioxxterms:version_of_record (DOI)

Note that the template input-forms.xml does not add every single field defined in RIOXX. For many of the fields declared as optional, you will need to modify the submission forms yourself. 
The standard DSpace submission forms already have an excess of different fields, this is why not all RIOXX optional fields were added by default. Even though these fields are not yet in the submission form they ARE being taken into account for the RIOXX OAI-PMH mapping. Please refer to the documentation of the mapping before enabling these fields in the submission form.

Following fields have to be included manually in the submission forms:

* dc:coverage
* dc:relation
* rioxxterms:apc

Continue the submission and don't forget to attach a file in order to create your first RIOXX test item and verify that it is completely "archived" in the repository. You can check this by verifying if the item now appears in the list of "Recent Submissions" on the repository homepage.

## OAI-PMH endpoint <a name="OAI-PMH-endpoint"></a>

Immediately after a new test item is available in the repository, it is NOT YET available in your OAI-PMH SOLR index.  
Normally, you have a nightly scheduled task (cron job) that synchronizes the archived items in the repository, with the OAI-PMH index.

For your testing purposes, you will want to verify new test items immediately. To do this, you need to manually trigger the OAI indexing task that populates the RIOXX OAI-PMH endpoint, as described in step 6 of the installation process.

After you have done this, you should be able to see your newly archived RIOXX test item through the link:

```
<server-url>/oai/rioxx?verb=ListRecords&metadataPrefix=rioxx
```

If you don't see your item there, check the corresponding troubleshooting section below.

**Rioxxterms:project**

There is a discrepancy between the examples listed in http://rioxx.net/v2-0-final/ and with the XSD definition for the exposure of funder_name and funder_id at [http://rioxx.net/schema/v2.0/rioxx/rioxxterms_.html#project](http://rioxx.net/schema/v2.0/rioxx/rioxxterms_.html#project)

In the DSpace RIOXX OAI-PMH endpoint, we have chosen to follow the XSD and to expose the rioxxterms: namespace for the funder_name and funder_id attributes.

# Troubleshooting <a name="Troubleshooting"></a>

## RIOXX test items are not visible in OAI-PMH endpoint <a name="RIOXX-test-OAI-PMH-endpoint"></a>

The RIOXX OAI-PMH endpoint has been developed in such a way that it only exposes items that are RIOXX compliant. An item will not appear there as long as not all of the following mandatory fields are present in the item:

* ali:license_ref
* dc:identifier that directly links to the attached bitstream (This can be both the bitstream as provided to DSpace or a URI to the full text publication hosted elsewhere)
* dc:language
* dc:title
* dcterms:dateAccepted
* rioxxterms:author
* rioxxterms:project
* rioxxterms:type
* rioxxterms:version

According to the specification, dc.source is mandatory where applicable (ISSN or ISBN). Currently, DSpace is not enforcing this in the OAI-PMH endpoint and will just expose ISSN or ISBN when they are present in the metadata.   
Again, aside from these metadatafields, make sure that the item contains a bitstream (file), or a value in the rioxxterms.openaccess.uri that links to the full text publication hosted elsewhere. Metadata records without bitstreams/openacces URI will not be exposed through the RIOXX OAI-PMH endpoint.


