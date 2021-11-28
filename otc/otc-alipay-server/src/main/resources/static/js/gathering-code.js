var gatheringCodeVM = new Vue({
	el : '#gatheringCode',
	data : {
		gatheringChannelCode: '',
		gatheringChannelDictItems: [],
		mediumDictItems: [],
		mediumDictBankItems: [],
		mediumDictBankTypeItems: [],
		qrcodeArray: [],
		gatheringCodeStateDictItems: [],
		accountTime: dayjs().format('YYYY-MM-DD'),
		gatheringCodes: [],
		mediums: [],
		qrcodeId: '',
		pageNum: 1,
		pageNum1: 1,
		totalPage: 1,
		totalPage1: 1,
		mediumCode: '',
		mediumId: '',
		editShow: false,
		mediumShow: true,
		qrcodeShow: false,
		addQrShow: false,
		showGatheringCodeFlag: true,
		mediumEdit: false,
		editGatheringCode: {//二维码处理
			gatheringChannelCode: '',
			fixedGatheringAmount : true,
			gatheringAmount : '',
		},
		medium : {//支付媒介处理
			mediumId: '',
			mediumNumber: '',
			mediumPhone: '',
			mediumHolder: '',
			bankcode: '',
			account: '',
			code: '',
			status: ''
		},
		showEditGatheringCodeFlag : false,
	},
	 filters: {
		 dateFilter: function (data, format = "") {
             var dt = new Date(data);
             var y = dt.getFullYear();
             var m = (dt.getMonth()+1).toString().padStart(2,"0");
             var d = dt.getDate().toString().padStart(2,"0");
             var h = dt.getHours().toString().padStart(2,"0");
             var mm = dt.getMinutes().toString().padStart(2,"0");
             var s = dt.getSeconds().toString().padStart(2,"0");
             if (format.toLocaleLowerCase() === "yyyy-mm-dd" ||
                 format.toLocaleLowerCase() === "") {
                 return `${y}-${m}-${d}`;
             } else if (format.toLocaleLowerCase() === "yyyy/mm/dd") {
                 return `${y}/${m}/${d}`;
             } else if (format.toLocaleLowerCase() === "yyyy-mm-dd hh:mm:ss") {
                 return `${y}-${m}-${d} ${h}:${mm}:${s}`;
             } else if (format.toLocaleLowerCase() === "yyyy/mm/dd hh:mm:ss") {
                 return `${y}/${m}/${d} ${h}:${mm}:${s}`;
             } else {
                 return `输入的时间格式有误`;
             }
         }
	 },
	computed : {},
	created : function() {
	},
	mounted : function() {
		var that = this;
		headerVM.title = '银行卡';
		headerVM.showBackFlag = true;
		that.loadGatheringChannelDictItem();
		that.loadGatheringBankCodelDictItem();
		that.loadMediumsByPage();


		$('.gathering-code-pic').on('fileuploaded', function (event, data, previewId, index) {
			that.qrcodeId = data.response.result.join(',');
			that.addQrcodeInfoSu();
		});

	},
	methods : {
		/**
		 * 获取收款渠道1
		 */
		loadGatheringChannelDictItem: function () {
			var that = this;
			that.$http.get('/recharge/findEnabledPayType').then(function (res) {
				this.gatheringChannelDictItems = res.body.result;
				this.mediumDictItems = [
					{mediumCode: 'card', mediumName: '银行卡'}
				]
			});
		},
		loadGatheringBankCodelDictItem: function () {
			var that = this;
			that.$http.get('/recharge/findEnabledPayType').then(function (res) {
				this.gatheringChannelDictItems = res.body.result;
				this.mediumDictBankItems = [
					{mediumCode: 'PSBC', mediumName: '邮储银行'},
					{mediumCode: 'CCB', mediumName: '建设银行'},
					{mediumCode: 'ICBC', mediumName: '工商银行'},
					{mediumCode: 'ABC', mediumName: '农业银行'},
					{mediumCode: 'CMB', mediumName: '招商银行'},
					{mediumCode: 'BOC', mediumName: '中国银行'},
					{mediumCode: 'CMBC', mediumName: '中国民生银行'},
					{mediumCode: 'CIB', mediumName: '兴业银行'},
					{mediumCode: 'BJBANK', mediumName: '北京银行'},
					{mediumCode: 'COMM', mediumName: '交通银行'},
					{mediumCode: 'CEB', mediumName: '中国光大银行'},
					{mediumCode: 'SPABANK', mediumName: '平安银行'},
                    {mediumCode: 'CITIC', mediumName: '中信银行'},
					{mediumCode: 'HXBANK', mediumName: '华夏银行'},
					{mediumCode: 'GCB', mediumName: '广州银行'},
					{mediumCode: 'SPDB', mediumName: '上海浦东发展银行'},
					{mediumCode: 'GLBANK', mediumName: '桂林银行'},
					{mediumCode: 'HXBANK', mediumName: '华夏银行'},
					{mediumCode: 'ZHHR', mediumName: '珠海华润银行'},
					{mediumCode: 'GDNX', mediumName: '广东农信'},
					{mediumCode: 'HBNX', mediumName: '湖北农信'},
					{mediumCode: 'HBNX', mediumName: '河北农信'},
					{mediumCode: 'HNNX', mediumName: '湖南农信'},
					{mediumCode: 'CZYH', mediumName: '沧州银行'},
					{mediumCode: 'HNYH', mediumName: '汉口银行'},
					{mediumCode: 'NMGYH', mediumName: '天津银行'},
					{mediumCode: 'SXXH', mediumName: '陕西信和'},
					{mediumCode: 'SHYH', mediumName: '上海银行'},
					{mediumCode: 'HRBYH', mediumName: '哈尔滨银行'},
					{mediumCode: 'SJYH', mediumName: '盛京银行'},
					{mediumCode: 'JLTH', mediumName: '吉林银行'},
					{mediumCode: 'SXXH', mediumName: '陕西信合'},
					{mediumCode: 'SHYH', mediumName: '上海银行'},
					{mediumCode: 'BJNS', mediumName: '北京农商'},
					{mediumCode: 'FJNX', mediumName: '福建农信'},
					{mediumCode: 'HRXJ', mediumName: '华融湘江'},
					{mediumCode: 'CQNCSY', mediumName: '重庆农村商业'},
					{mediumCode: 'HZYH', mediumName: '杭州银行'},
					{mediumCode: 'HZYH', mediumName: '浙江农信'},
					{mediumCode: 'SRYH', mediumName: '上饶银行'},
					{mediumCode: 'SCNX', mediumName: '四川农信'},
					{mediumCode: 'NJYH', mediumName: '龙江银行'},
					{mediumCode: 'SCNX', mediumName: '黑农江农信'},
					{mediumCode: 'GZYH', mediumName: '贵州银行'},
					{mediumCode: 'NBYH', mediumName: '宁波银行'},
					{mediumCode: 'GFYH', mediumName: '广发银行'},
					{mediumCode: 'CXSXYH', mediumName: '重庆三峡银行'},
					{mediumCode: 'QZYH', mediumName: '泉州银行'},
					{mediumCode: 'DWNS', mediumName: '东莞农商'},
					{mediumCode: 'HFYH', mediumName: '恒丰银行'},
					{mediumCode: 'GXNX', mediumName: '广西农信'},
					{mediumCode: 'CSYH', mediumName: '长沙银行'},
					{mediumCode: 'GDNX', mediumName: '广东农信'},
					{mediumCode: 'HNNX', mediumName: '河南农信'},
					{mediumCode: 'HSYH', mediumName: '甘肃银行'},
					{mediumCode: 'LZYH', mediumName: '兰州银行'},
					{mediumCode: 'HLNXS', mediumName: '海南农信社'},
					{mediumCode: 'WSYHS', mediumName: '徽商银行'},
					{mediumCode: 'SCTFYH', mediumName: '四川天府银行'},
					{mediumCode: 'SCYHS', mediumName: '四川银行'},
					{mediumCode: 'ZYYHK', mediumName: '中原银行'},
					{mediumCode: 'LZYH', mediumName: '泸州银行'},
					{mediumCode: 'JSNXS', mediumName: '江苏农信'},
					{mediumCode: 'SXNXS', mediumName: '山西农信'}
				]
				this.mediumDictBankTypeItems = [
					{mediumCode: 'W', mediumName: '出款卡'},
					{mediumCode: 'R', mediumName: '入款卡'},
				]
			});
		},
		query1: function () {
			this.pageNum1 = 1;
			this.loadMediumsByPage();
		},

		prePage1: function () {
			this.pageNum1 = this.pageNum1 - 1;
			this.loadMediumsByPage();
		},

		nextPage1: function () {
			this.pageNum1 = this.pageNum1 + 1;
			this.loadMediumsByPage();
		},

		loadMediumsByPage: function () {
			var that = this;
			that.$http.get('/statisticalAnalysis/findMediumsByPage', {
				params : {
					pageSize : 5,
					pageNum : that.pageNum1,
					status : that.medium.status
				}
			}).then(function(res) {
				that.mediums = res.body.result.content;
				that.pageNum1 = res.body.result.pageNum;
				that.totalPage1 = res.body.result.totalPage;
				
			});
		},

		query : function() {
			this.pageNum = 1;
			this.loadGatheringCodeByPage();
		},

		prePage : function() {
			this.pageNum = this.pageNum - 1;
			this.loadGatheringCodeByPage();
		},

		nextPage : function() {
			this.pageNum = this.pageNum + 1;
			this.loadGatheringCodeByPage();
		},

		loadGatheringCodeByPage : function() {
			var that = this;
			that.$http.get('/statisticalAnalysis/findMyGatheringCodeByPage', {
				params : {
					pageSize : 5,
					pageNum : that.pageNum,
					status : that.status,
					gatheringChannelCode : that.gatheringChannelCode
				}
			}).then(function(res) {
				that.gatheringCodes = res.body.result.content;
				that.pageNum = res.body.result.pageNum;
				that.totalPage = res.body.result.totalPage;
			});
		},
	/*	addData: function(){
			var that = this;
			if(!that.editGatheringCode.qrcodeNumber)
				return;
			that.$http.get('/qrcode/findQrByAccount', {
				params : {
					qrcodeNumber : that.editGatheringCode.qrcodeNumber
				}
			}).then(function(res) {
				if(res.body.success){
					that.editGatheringCode.payee = res.body.result.retain1
					that.editGatheringCode.pid =  res.body.result.pid
					that.editGatheringCode.phone = res.body.result.phone
				}else{
					
				}
			});
		}, */
		initFileUploadWidget : function(storageId) {
			var initialPreview = [];
			var initialPreviewConfig = [];
			if (storageId != null) {
				initialPreview.push('/storage/fetch/' + storageId);
				initialPreviewConfig.push({
					downloadUrl : '/storage/fetch/' + storageId
				});
			}
			$('.gathering-code-pic').fileinput('destroy').fileinput({
				browseOnZoneClick : true,
				showBrowse : false,
				showCaption : false,
				showClose : true,
				showRemove : false,
				showUpload : false,
				dropZoneTitle : '点击选择图片',
				dropZoneClickTitle : '',
				layoutTemplates : {
					footer : ''
				},
				maxFileCount : 1,
				uploadUrl : '/storage/uploadPic',
				enctype : 'multipart/form-data',
				allowedFileExtensions : [ 'jpg', 'png', 'bmp', 'jpeg' ],
				initialPreview : initialPreview,
				initialPreviewAsData : true,
				initialPreviewConfig : initialPreviewConfig
			});
		},
		switchGatheringAmountMode : function() {
			if (!this.editGatheringCode.fixedGatheringAmount)
				this.editGatheringCode.gatheringAmount = '';
		},
		showEditMediumPage : function(mediumId){
			var that = this;
			if (mediumId == null || mediumId == '') {//添加收款媒介
				that.medium = {
					mediumId: '',
					mediumNumber: '',
					mediumPhone: '',
					mediumHolder: '',
					bankcode: '',
					code: '',
					status: ''
				};
				that.showEditMediumCodePageInner();
			} else {//编辑收款媒介
				that.$http.get('/statisticalAnalysis/findMyMediumById', {
					params : {
						mediumId : mediumId,
					}
				}).then(function(res) {
					that.medium = res.body.result;
					that.showEditMediumCodePageInner('edit');
				});
			}
		},
		addQrInfo : function(){
			var that = this;
			if (that.editGatheringCode.fixedGatheringAmount == null) {
				layer.alert('请选择是否固定收款金额', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (that.editGatheringCode.fixedGatheringAmount) {
				if (that.editGatheringCode.gatheringAmount == null || that.editGatheringCode.gatheringAmount == '') {
					layer.alert('请输入收款金额', {
						title : '提示',
						icon : 7,
						time : 3000
					});
					return;
				}
			}
			$('.gathering-code-pic').fileinput('upload');
			if ($('.gathering-code-pic').fileinput('getPreview').content.length != 0) {
			} else {
				var filesCount = $('.gathering-code-pic').fileinput('getFilesCount');
				if (filesCount == 0) {
					layer.alert('请选择要上传的图片', {
						title : '提示',
						icon : 7,
						time : 3000
					});
					return;
				}
				$('.gathering-code-pic').fileinput('upload');
			}
			var filesCount = $('.gathering-code-pic').fileinput('getFilesCount');
			if (filesCount == 0) {
				layer.alert('请选择要上传的图片', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
		},
		addQrcodeInfoSu : function(){
			var that = this;
			var qrcodeId = that.qrcodeId;
			var flag = that.editGatheringCode.fixedGatheringAmount;
			if(qrcodeId == ''  || qrcodeId == null	){
					layer.alert('请上传二维码', {
						title : '提示',
						icon : 7,
						time : 3000
					});
					return;
			}
			var mediumId = that.mediumId; 
			var amount = '';
			if(that.editGatheringCode.fixedGatheringAmount)
				 amount = that.editGatheringCode.gatheringAmount;
			that.$http.get('/statisticalAnalysis/addQrInfo',  {
				params : {
					qrcodeId : qrcodeId,
					mediumId : mediumId,
					amount : amount,
					flag : flag
			}} ).then(function(res) {
				if(res.body.success){
					layer.alert('操作成功!', {
						icon : 1,
						time : 3000,
						shade : false
					});
					that.showQrManage(that.mediumId);
					that.forQrManage();
				}else{
					layer.alert(res.body.message, {
						icon : 1,
						time : 3000,
						shade : false
					});
				}
			});
		},
		addQr: function () {//添加收款码
			headerVM.showBackFlag = false;
			headerVM.title = '添加收款码';
			this.mediumShow = false;
			this.mediumEdit = false;
			this.qrcodeShow = false;
			this.addQrShow = true;
			this.initFileUploadWidget();
		},
		reply: function () {
			headerVM.showBackFlag = true;
			headerVM.title = '银行卡';
			this.mediumShow = true;
			this.mediumEdit = false;
			this.qrcodeShow = false;
		},
		forQrManage: function () {
			headerVM.showBackFlag = false;
			headerVM.title = '我的银行卡';
			this.mediumShow = false;//关闭主页面
			this.mediumEdit = false;//打开编辑媒介页面
			this.qrcodeShow = true;//二维码编辑页面关闭
			this.addQrShow = false;//二维码编辑页面关闭
		},
		showEditGatheringCodePageInner: function () {
			headerVM.showBackFlag = false;
			headerVM.title = '添加收款码';
			this.showGatheringCodeFlag = false;//展示所有二维码数据
			this.showEditGatheringCodeFlag = true;//单张二维码数据
		},
		showEditMediumCodePageInner : function(edit) {
			headerVM.showBackFlag = false;
			if(edit){
				this.editShow = true;			
				headerVM.title = '编辑收款媒介';
			}else{
				this.editShow = false;	
				headerVM.title = '添加收款媒介';
			}
			this.mediumShow = false;//关闭主页面
			this.mediumEdit = true;//打开编辑媒介页面
		},
		hideEditGatheringCodePage : function() {
			headerVM.showBackFlag = true;
			headerVM.title = '收款码';
			this.mediumEdit = false;
			this.mediumShow = true;
			this.qrcodeShow = false; 
			this.addQrShow = false; 
		},
		addMedium : function (){
			var that = this;
			var medium = that.medium;
			if (medium.code == null || medium.code == '') {
				layer.alert('请选择收款媒介', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (medium.mediumNumber == null || medium.mediumNumber == '') {
				layer.alert('请填写登录账户', {
					title : '提示',
					icon : 7,
					time : 3000
				});
				return;
			}
			if (medium.mediumHolder == null || medium.mediumHolder == '') {
				layer.alert('请填写收款人', {
					title: '提示',
					icon: 7,
					time: 3000
				});
                return;
            }
            if (medium.mediumPhone == null || medium.mediumPhone == '') {
                layer.alert('请填写关联手机号/邮箱/编号', {
                    title: '提示',
                    icon: 7,
                    time: 3000
                });
                return;
            }
            /*if (medium.bankcode == null || medium.bankcode == '') {
                layer.alert('请选择银行卡类型/出款/入款', {
                    title: '提示',
                    icon: 7,
                    time: 3000
                });
                return;
            }*/
            if (medium.account == null || medium.account == '') {
                layer.alert('请选择银行卡开户行', {
                    title: '提示',
					icon: 7,
					time: 3000
				});
				return;
			}
			if (that.medium.mediumId == '' || that.medium.mediumId == null) {//媒介系统编号为NULL则是新增
				that.$http.post('/statisticalAnalysis/addMedium', that.medium).then(function (res) {
					if (res.body.success) {
						layer.alert('操作成功!', {
							icon: 1,
							time: 3000,
							shade: false
						});
						that.reply();
						that.loadMediumsByPage();
					} else {
						layer.alert(res.body.message, {
							icon: 1,
							time : 3000,
							shade : false
						});
						that.reply();
						that.loadMediumsByPage();
					}
				});
			}else{//修改媒介
				that.$http.post('/statisticalAnalysis/editMedium',  that.medium ).then(function(res) {
					if(res.body.success){
						layer.alert('操作成功!', {
							icon : 1,
							time : 3000,
							shade : false
						});
						that.reply();
						that.loadMediumsByPage();
					}else{
						layer.alert(res.body.message, {
							icon : 1,
							time : 3000,
							shade : false
						});
						that.reply();
						that.loadMediumsByPage();
					}
				});
			}
		},
		showQrManage:function(mediumId){//二维码管理
			//获取到所有的二维码
			var that = this;
			var data = { mediumId :mediumId}
			that.$http.post('/statisticalAnalysis/findQrByMediumId', mediumId ).then(function(res) {//查询当前收款媒介所有的二维码
				if(res.body.success){
					that.qrcodeArray = res.body.result;
					that.qrManage();
					that.mediumId = mediumId;//当前收款媒介
				}else{
					layer.alert(res.body.message, {
						icon : 1,
						time : 3000,
						shade : false
					});
				}
			});
		},
		qrManage:function(){//二维码管理
			headerVM.showBackFlag = false;
			headerVM.title = '我的收款码';
			this.mediumShow = false;//关闭主页面
			this.mediumEdit = false;//打开编辑媒介页面
			this.qrcodeShow = true;
		},
		delGatheringCode : function(gatheringCodeId) {
			var that = this;
			layer.msg('确认这么做', {
			  	time: 0 //不自动关闭
			  ,btn: ['确认删除', '点错了']
			  ,yes: function(index){
			that.$http.get('/statisticalAnalysis/delMyGatheringCodeById', {
				params : {
					id : gatheringCodeId,
				}
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.hideEditGatheringCodePage();
				that.query();
				that.query1();
			});
		 }
	});
		},
		dealeteQr : function(qrId){
			var that = this;
			layer.msg('确认这么做', {
			  	time: 0 //不自动关闭
			  ,btn: ['确认删除', '点错了']
			  ,yes: function(index){
			that.$http.get('/statisticalAnalysis/dealeteQr', {
				params : {
					qrcodeId : qrId,
				}
			}).then(function(res) {
				if(res.body.success){
					layer.msg(res.body.message);
				}else{
					layer.msg(res.body.message);
				}
				that.showQrManage(that.mediumId);
			});
			
		 }
	});
		},
		delMedium : function(mediumId){
			var that = this;
			layer.msg('确认这么做', {
			  	time: 0 //不自动关闭
			  ,btn: ['确认删除', '点错了']
			  ,yes: function(index){
			that.$http.get('/statisticalAnalysis/delMedium', {
				params : {
					mediumId : mediumId,
				}
			}).then(function(res) {
				layer.alert('操作成功!', {
					icon : 1,
					time : 3000,
					shade : false
				});
				that.reply();
				that.loadMediumsByPage();
			});
		 }
	});
		}
	}
});