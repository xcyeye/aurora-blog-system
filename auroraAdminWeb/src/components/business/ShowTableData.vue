<template>
  <div>
		<n-space vertical>
			<n-card :bordered="props.dataTableInfo.bordered" :title="props.dataTableInfo.title" class="h-full shadow-sm rounded-16px">
				<!-- 数据展示相关 -->
				<n-space v-if="showTableData" vertical>
					<n-data-table
						:loading="dataTableLoadingStatus"
						:remote="true"
						:scroll-x="props.dataTableInfo.scrollX"
						:striped="props.dataTableInfo.striped"
						class="h-480px"
						:flex-height="true"
						:row-key="getRowKey"
						:columns="dataTableColumns"
						:data="dataTableRowData"
						:render-expand-icon="renderExpandIcon"
						:pagination="pagination"
						@update:page="handleChangePageNum"
						@update:page-size="handleChangePageSize"
						@update-sorter="handleSorterChange"
						@update:checked-row-keys="handleCheckedRowKeys"/>
				</n-space>
				<n-space v-if="!showTableData" vertical>
					<slot name="content"/>
				</n-space>
				<template #header-extra>
					<n-space justify="end">
						<slot name="cardHeader1"/>
						<slot name="cardHeader2"/>
						<slot name="cardHeader3"/>
						<slot name="cardHeader4"/>
						<slot name="cardHeader5"/>
						<slot name="cardHeader6"/>
					</n-space>
				</template>
			</n-card>
		</n-space>
  </div>
</template>

<script lang="ts" setup>
import {DataTableSortState, PaginationProps, PaginationSizeOption} from "naive-ui";
import {RowData, TableColumn} from "naive-ui/es/data-table/src/interface";
import {onBeforeMount, onMounted, ref, VNode} from "vue";
import {Condition, PageData} from "@/bean/core/bean";
import {EnumMittEventName} from "@/enum";
import {emitter} from "@/utils";
import RequestResult = Service.RequestResult;

// 定义emit
const emits = defineEmits(['handleChangePageSize', 'handleChangePageNum', 'handleCheckedRowKeys'])

interface DataTableInfo {
	title?: string,
	rowKey: string,
	striped?: boolean,
	scrollX?: number,
	bordered?: boolean
}

interface Props {
	/** 和加载数据展示页相关的属性 */
	dataTableColumns: Array<TableColumn>,
	pageSizes?: number[],
	dataTableInfo: DataTableInfo,
	renderExpandIcon?: () => VNode,
	originCondition?: Condition,
	queryDataMethod: (condition: Condition) => Promise<RequestResult<PageData<RowData>>>,
	showTableData?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	dataTableInfo: () => {
		return {
			rowKey: '',
			striped: true,
			scrollX: undefined,
			bordered: true
		}
	},
	showTableData: true
});

// 定义data
const dataTableRowData = ref<Array<RowData>>([])
const dataTableLoadingStatus = ref<boolean>(true)
const pagination = ref<PaginationProps>({
	pageSize: 20,
	page: 1,
	pageCount: 0,
	showSizePicker: true,
	pageSizes: [10, 20, 30],
})
const queryCondition = ref<Condition>({})
const requestDataStatus = ref(false)

// 方法
const loadData = (pageSize: number | null, pageNum: number | null, orderBy: string | null, order: string | null) => {
	// if (!requestDataStatus.value) return
	if (!dataTableLoadingStatus.value) dataTableLoadingStatus.value = true
	if (pageSize) {
		pagination.value.pageSize = pageSize
		queryCondition.value.pageSize = pagination.value.pageSize
	}
	if (pageNum) {
		pagination.value.page = pageNum
		queryCondition.value.pageNum = pagination.value.page
	}
	if (orderBy && order) {
		let orderByStr = orderBy
		if (order === 'ascend') {
			orderByStr = orderByStr + " asc"
		}else if (order === 'descend') {
			orderByStr = orderByStr + " desc"
		}
		queryCondition.value.orderBy = orderByStr
	}
	props.queryDataMethod(queryCondition.value).then(result => {
		dataTableLoadingStatus.value = false
		if (result.data) {
			if (result.data.result) {
				dataTableRowData.value = result.data.result
				pagination.value.pageCount = result.data.pages!
				pagination.value.pageSize = result.data.pageSize!
			}else {
				dataTableRowData.value = []
			}
		}else {
			dataTableRowData.value = []
		}
	})
}

const handleChangePageSize = (pageSize: number):void => {
	loadData(pageSize, null, null, null);
	pagination.value.page = 1
	emits('handleChangePageSize', pageSize);
}

const handleChangePageNum = (page: number):void => {
	loadData(null, page, null, null)
	pagination.value.page = page
	emits('handleChangePageNum', page);
}

const getRowKey = (rowData: object): string => {
	if (rowData.hasOwnProperty(props.dataTableInfo.rowKey)) {
		// @ts-ignore
		return rowData[props.dataTableInfo.rowKey]
	}
	return ''
}

const handleCheckedRowKeys = (keys: Array<string>): undefined => {
	emits('handleCheckedRowKeys', keys);
	return undefined
}

const handleSorterChange = (sorter: DataTableSortState): undefined => {
	props.dataTableColumns.forEach((column) => {
		// @ts-ignore
		if (column.sortOrder === undefined) return
		if (!sorter) {
			// @ts-ignore
			column.sortOrder = false
			return;
		}
		// @ts-ignore
		if (column.key === sorter.columnKey) {
			// @ts-ignore
			column.sortOrder = sorter.order
			// @ts-ignore
			loadData(null, null, column.key as string, sorter.order as string)
		}else {
			// @ts-ignore
			column.sortOrder = false
		}
	})
	return undefined
}

const assertPageSizes = () => {
	if (props.pageSizes) {
		pagination.value.pageSizes = props.pageSizes
		if (pagination.value.pageSizes.indexOf(pagination.value.pageSize!) === -1) {
			pagination.value.pageSizes.push(pagination.value.pageSize!)
			// @ts-ignore
			pagination.value.pageSizes.sort((a: PaginationSizeOption, b: PaginationSizeOption) => (a.value - b.value))
		}
	}
}

// 挂载emit
onMounted(() => {
	emitter.on(EnumMittEventName.globalSearchCondition, event => {
		if (event) {
			requestDataStatus.value = true
			queryCondition.value = event as Condition;
			loadData(null, null, null, null)
		}
	})
	emitter.on(EnumMittEventName.reloadData, e => {
		requestDataStatus.value = true
		loadData(null, null, null, null)
	})
	emitter.on(EnumMittEventName.resetGlobalSearchCondition, e => {
		requestDataStatus.value = true
		queryCondition.value = e as Condition
		loadData(null, null, null, null)
	})
})

onBeforeMount(() => {
	assertPageSizes()
	loadData(null, null, null, null)
})

</script>

<style scoped></style>
