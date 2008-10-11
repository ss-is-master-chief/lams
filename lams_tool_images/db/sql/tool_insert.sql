# Connection: ROOT LOCAL
# Host: localhost
# Saved: 2005-04-07 10:42:43
# 
INSERT INTO lams_tool
(
tool_signature,
service_name,
tool_display_name,
description,
tool_identifier,
tool_version,
learning_library_id,
default_tool_content_id,
valid_flag,
grouping_support_type_id,
supports_run_offline_flag,
learner_url,
learner_preview_url,
learner_progress_url,
author_url,
monitor_url,
define_later_url,
export_pfolio_learner_url,
export_pfolio_class_url,
contribute_url,
moderation_url,
help_url,
language_file,
classpath_addition,
context_file,
create_date_time,
modified_date_time
)
VALUES
(
'laimag10',
'imageGalleryService',
'Shared ImageGallery',
'Shared ImageGallery',
'sharedimageGallery',
'@tool_version@',
NULL,
NULL,
0,
2,
1,
'tool/laimag10/learning/start.do?mode=learner',
'tool/laimag10/learning/start.do?mode=author',
'tool/laimag10/learning/start.do?mode=teacher',
'tool/laimag10/authoring/start.do',
'tool/laimag10/monitoring/summary.do',
'tool/laimag10/definelater.do',
'tool/laimag10/exportPortfolio?mode=learner',
'tool/laimag10/exportPortfolio?mode=teacher',
'tool/laimag10/contribute.do',
'tool/laimag10/moderate.do',
'http://wiki.lamsfoundation.org/display/lamsdocs/laimag10',
'org.lamsfoundation.lams.tool.imageGallery.ApplicationResources',
'lams-tool-laimag10.jar',
'/org/lamsfoundation/lams/tool/imageGallery/imageGalleryApplicationContext.xml',
NOW(),
NOW()
)
