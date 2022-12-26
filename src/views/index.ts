import type { RouteComponent } from 'vue-router';

export const views: Record<RouterPage.LastDegreeRouteKey, RouteComponent | (() => Promise<RouteComponent>)> = {
  403: () => import('./_builtin/403/index.vue'),
  404: () => import('./_builtin/404/index.vue'),
  500: () => import('./_builtin/500/index.vue'),
  'constant-page': () => import('./_builtin/constant-page/index.vue'),
  login: () => import('./_builtin/login/index.vue'),
  'not-found': () => import('./_builtin/not-found/index.vue'),
  article_bulletin: () => import('./article/bulletin/index.vue'),
  'article_category-tag': () => import('./article/category-tag/index.vue'),
  article_edit: () => import('./article/edit/index.vue'),
  'article_friend-link_friend-link-list': () => import('./article/friend-link/friend-link-list/index.vue'),
  'article_friend-link_friend-link-modify': () => import('./article/friend-link/friend-link-modify/index.vue'),
  'article_friend-link': () => import('./article/friend-link/index.vue'),
  article_manage: () => import('./article/manage/index.vue'),
  article_share: () => import('./article/share/index.vue'),
  'auth-demo_permission': () => import('./auth-demo/permission/index.vue'),
  'auth-demo_super': () => import('./auth-demo/super/index.vue'),
  'auth-server_login': () => import('./auth-server/login/index.vue'),
  'auth-server_login_login-info-list': () => import('./auth-server/login/login-info-list/index.vue'),
  'auth-server_login_login-info-log': () => import('./auth-server/login/login-info-log/index.vue'),
  'auth-server_oauth': () => import('./auth-server/oauth/index.vue'),
  'auth-server_oauth_oauth-client-list': () => import('./auth-server/oauth/oauth-client-list/index.vue'),
  'auth-server_oauth_oauth-client-modify': () => import('./auth-server/oauth/oauth-client-modify/index.vue'),
  component_table: () => import('./component/table/index.vue'),
  dashboard_analysis: () => import('./dashboard/analysis/index.vue'),
  dashboard_workbench: () => import('./dashboard/workbench/index.vue'),
  exception_403: () => import('./exception/403/index.vue'),
  exception_404: () => import('./exception/404/index.vue'),
  exception_500: () => import('./exception/500/index.vue'),
  'file-center_all-file_all-file-list': () => import('./file-center/all-file/all-file-list/index.vue'),
  'file-center_all-file_all-file-show': () => import('./file-center/all-file/all-file-show/index.vue'),
  'file-center_all-file': () => import('./file-center/all-file/index.vue'),
  'file-center_picture': () => import('./file-center/picture/index.vue'),
  'function_tab-detail': () => import('./function/tab-detail/index.vue'),
  'function_tab-multi-detail': () => import('./function/tab-multi-detail/index.vue'),
  function_tab: () => import('./function/tab/index.vue'),
  'message-center_email-log': () => import('./message-center/email-log/index.vue'),
  'message-center_email-manage': () => import('./message-center/email-manage/index.vue'),
  'message-center_email-manage_user-list': () => import('./message-center/email-manage/user-list/index.vue'),
  'message-center_email-manage_user-modify': () => import('./message-center/email-manage/user-modify/index.vue'),
  'message-center_mq-message': () => import('./message-center/mq-message/index.vue'),
  'message-center_send-mail': () => import('./message-center/send-mail/index.vue'),
  monitor_nacos: () => import('./monitor/nacos/index.vue'),
  monitor_rabbitmq: () => import('./monitor/rabbitmq/index.vue'),
  monitor_sentinel: () => import('./monitor/sentinel/index.vue'),
  'multi-menu_first_second-new_third': () => import('./multi-menu/first/second-new/third/index.vue'),
  'multi-menu_first_second': () => import('./multi-menu/first/second/index.vue'),
  permission_interface: () => import('./permission/interface/index.vue'),
  'permission_permission-manage': () => import('./permission/permission-manage/index.vue'),
  permission_role: () => import('./permission/role/index.vue'),
  plugin_charts_antv: () => import('./plugin/charts/antv/index.vue'),
  plugin_charts_echarts: () => import('./plugin/charts/echarts/index.vue'),
  plugin_copy: () => import('./plugin/copy/index.vue'),
  plugin_editor_markdown: () => import('./plugin/editor/markdown/index.vue'),
  plugin_editor_quill: () => import('./plugin/editor/quill/index.vue'),
  plugin_icon: () => import('./plugin/icon/index.vue'),
  plugin_map: () => import('./plugin/map/index.vue'),
  plugin_print: () => import('./plugin/print/index.vue'),
  plugin_swiper: () => import('./plugin/swiper/index.vue'),
  plugin_video: () => import('./plugin/video/index.vue'),
  test_test1: () => import('./test/test1/index.vue'),
  test_test2: () => import('./test/test2/index.vue'),
  test_test3: () => import('./test/test3/index.vue'),
  user_manage: () => import('./user/manage/index.vue'),
  'user_manage_user-list': () => import('./user/manage/user-list/index.vue'),
  'user_manage_user-modify': () => import('./user/manage/user-modify/index.vue'),
  user_profile: () => import('./user/profile/index.vue'),
  'user_profile_user-info': () => import('./user/profile/user-info/index.vue'),
  'user_profile_user-security': () => import('./user/profile/user-security/index.vue')
};
