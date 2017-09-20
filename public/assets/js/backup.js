t.fileSelected=function(n){if(n&&n.length){var r;t.files={};for(var i in n)
r=angular.copy(t.field().uploadInformation()),
r.file=n[i],r.url=r.url+t.$parent.entry.values.id,
e.upload(r).progress(function(e){t.files[e.config.file.name]={name:e.config.file.name,progress:Math.min(100,parseInt(100*e.loaded/e.total))}})
.success(function(e,n,r,i){
var d = new Date(); $('#prodImg').attr('src', '/image/product/' + t.$parent.entry.values.id + '?'+d.getTime());
$('#mediaImg').attr('src', '/media/image/' + t.$parent.entry.values.code + '?'+d.getTime());
$('#reviewImg').attr('src', '/review/image/' + t.$parent.entry.values.id + '?'+d.getTime());
if(t.files[i.file.name]={name:t.apifilename?e[t.apifilename]:i.file.name,progress:0},t.apifilename){var a=Object.keys(t.files).map(function(e){return t.files[e].name});t.value=a.join(",")}else t.value=Object.keys(t.files).join(",")}).error(function(e,n,r,i){delete t.files[i.file.name],t.value=Object.keys(t.files).join(",")})}},



n.prototype.submitEdition=function(e){
var t=this;
if(e.preventDefault(),this.validateEntry()){
var n=this.view,r=this.$scope.entry.transformToRest(n.fields());
this.progression.start(),
this.WriteQueries.updateOne(n,r,this.originEntityId)
.then(function(){
    t.progression.done(),t.notification.log("Изменения сохранены.",{addnCls:"humane-flatty-success"}),
    //$state.go($state.get('edit'), { entity: entity.name(), entry: $scope.entry});
    if (n.entity._name == 'orders')
            location.reload();
},
this.handleError.bind(this))}
