<template>
	<div>
		<aurora-common :is-show-top-img="true" :user-uid="userUid">
			<template #center1>
				<aurora-card>
					<aurora-archive :article-arr="articleArr" />
				</aurora-card>
			</template>
		</aurora-common>
	</div>
</template>

<script lang="ts" setup>
import {defineComponent, onBeforeMount, ref} from "vue";
import {setMetaDescription, setMetaKeywords, setMetaTitle, StringUtil} from "@/utils";
import {useRouter} from "vue-router";
import {useRouterPush} from "@/composables";
import {articleApi} from "@/service";
import {ArticleVo} from "@/bean/vo/article/ArticleVo";
import {useUserInfo} from "@/stores";
import {useMeta} from "vue-meta";

defineComponent({name: 'index'});

const userUid = ref<string>('')
const router = useRouter()
const routerPush = useRouterPush()
const articleArr = ref<Array<ArticleVo>>([])

const getRouterParams = () => {
	userUid.value = router.currentRoute.value.params.userUid as string
	if (!StringUtil.haveLength(userUid.value)) {
		routerPush.routerPush({
			name: 'home'
		})
	}
	
	const userInfo = useUserInfo().getUserInfo(userUid.value)
	
	// 获取文章数据
	articleApi.queryListDataByCondition({status: true, delete: false, otherUid: userUid.value, pageSize: 99999}).then(result => {
		if (result.data && result.data.result) {
			articleArr.value = result.data.result
			setMetaTitle(`${userInfo.username} - 时间轴`)
			setMetaDescription(`${userInfo.username} - 时间轴`)
			setMetaKeywords(`${userInfo.username} - 时间轴`)
		}
	})
}
getRouterParams()
</script>

<style scoped>

</style>