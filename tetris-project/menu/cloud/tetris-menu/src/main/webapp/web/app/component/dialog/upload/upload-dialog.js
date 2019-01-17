/**
 * Created by lvdeyang on 2018/11/28 0028.
 */
define([
    'text!' + window.APPPATH + 'component/dialog/upload/upload-dialog.html',
    'restfull',
    'jquery',
    'file',
    'vue',
    'element-ui',
    'css!' + window.APPPATH + 'component/dialog/upload/upload-dialog.css'
], function(tpl, ajax, $, File, Vue){

    var pluginName = 'mi-upload-dialog';

    var FILE_SELECTED = 'file-selected';

    var MATERIAL_TYPE_IMAGE = 'image';

    var MATERIAL_TYPE_AUDIO = 'audio';

    var MATERIAL_TYPE_VIDEO = 'video';

    var MATERIAL_TYPE_TXT = 'txt';

    var requires = [];

    function Type(mimetype, suffix, type){
        this.mimetype = mimetype;
        this.suffix = suffix;
        this.type = type;
    };

    Type.prototype.equals = function(mimetype){
        return this.mimetype === mimetype;
    };

    requires.push(new Type('image/jpeg', 'jpg', MATERIAL_TYPE_IMAGE));
    requires.push(new Type('image/png', 'png', MATERIAL_TYPE_IMAGE));
    requires.push(new Type('image/gif', 'gif', MATERIAL_TYPE_IMAGE));
    requires.push(new Type('audio/mpeg', 'mp3', MATERIAL_TYPE_AUDIO));
    requires.push(new Type('video/mp4', 'mp4', MATERIAL_TYPE_VIDEO));
    requires.push(new Type('text/plain', 'txt', MATERIAL_TYPE_TXT));

    var isRequiresType = function(mimetype){
        for(var i=0; i<requires.length; i++){
            if(requires[i].equals(mimetype)){
                return true;
            }
        }
        return false;
    };

    var translateSuffix = function(mimetype){
        for(var i=0; i<requires.length; i++){
            if(requires[i].equals(mimetype)){
                return requires[i].suffix;
            }
        }
        return null;
    };

    Vue.component(pluginName, {
        template: tpl,
        data:function(){
            return {
                visible:false,
                files:[]
            }
        },
        methods:{
            //开启对话框
            open:function(){
                var self = this;
                self.visible = true;
            },
            //对话框关闭初始化数据
            closed:function(){
                var self = this;
                if(self.files.length > 0){
                    self.files.splice(0, self.files.length);
                }
            },
            //空间选择文件
            fileSelected:function(e){
                var self = this;
                var $input = e.target;
                var files = $input.files;
                for(var i=0; i<files.length; i++){
                    var file = files[i];
                    if(isRequiresType(files[i].type)){
                        var finded = false;
                        for(var j=0; j<self.files.length; j++){
                            var exist = self.files[j];
                            if(exist.lastModified===file.lastModified &&
                                exist.name===file.name &&
                                exist.size===file.size &&
                                exist.type===file.type){
                                finded = true;
                                break;
                            }
                        }
                        if(!finded) self.files.push(files[i]);
                    }
                }
                $($input).val('');
            },
            //选择文件按钮事件
            selectFile:function(e){
                var $button = $(e.target).closest('.el-button');
                var $input = $button.find('input[type=file]');
                $input.click();
            },
            //移除文件
            removeFile:function(scope){
                var self = this;
                var row = scope.row;
                for(var i=0; i<self.files.length; i++){
                    if(self.files[i] === row){
                        self.files.splice(i, 1);
                        return;
                    }
                }
            },
            //选择文件
            handleOkClick:function(){
                var self = this;
                if(self.files.length <= 0) return;
                var done = function(){
                    self.visible = false;
                };
                self.$emit(FILE_SELECTED, self.files, done);
            },
            //计算文件大小
            formatSize:function(size){
                return File.prototype.formatSize(size);
            },
            //获取文件类型
            type:function(type){
                return translateSuffix(type);
            },
            //获取文件类型描述
            requireTypes:function(){
                var summary = '支持格式：';
                for(var i=0; i<requires.length; i++){
                    summary += requires[i].suffix;
                    if(i !== requires.length-1){
                        summary += ', ';
                    }
                }
                return summary;
            },
            //input类型过滤
            accept:function(){
                var accept = '';
                for(var i=0; i<requires.length; i++){
                    accept += requires[i].mimetype;
                    if(i !== requires.length-1){
                        accept += ',';
                    }
                }
                return accept;
            },
            //判断是否是图片类型
            isImage:function(mimetype){
                for(var i=0; i<requires.length; i++){
                    if(requires[i].mimetype===mimetype && requires[i].type===MATERIAL_TYPE_IMAGE){
                        return true;
                    }
                }
                return false;
            },
            //判断是否是音频类型
            isAudio:function(mimetype){
                for(var i=0; i<requires.length; i++){
                    if(requires[i].mimetype===mimetype && requires[i].type===MATERIAL_TYPE_AUDIO){
                        return true;
                    }
                }
                return false;
            },
            //判断是否是视频类型
            isVideo:function(mimetype){
                for(var i=0; i<requires.length; i++){
                    if(requires[i].mimetype===mimetype && requires[i].type===MATERIAL_TYPE_VIDEO){
                        return true;
                    }
                }
                return false;
            },
            //判断是否是文本类型
            isTxt:function(mimetype){
                for(var i=0; i<requires.length; i++){
                    if(requires[i].mimetype===mimetype && requires[i].type===MATERIAL_TYPE_TXT){
                        return true;
                    }
                }
                return false;
            }
        }
    });

});
