/*
 * AI漫剪
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const aiClipApi = {
  workspace: () => getRequest('/media/ai-clip/workspace'),
  queryProject: (param) => postRequest('/media/ai-clip/project/query', param),
  projectDetail: (id) => getRequest(`/media/ai-clip/project/detail/${id}`),
  saveProject: (param) => postRequest('/media/ai-clip/project/save', param),
  queryMaterial: (param) => postRequest('/media/ai-clip/material/query', param),
  uploadMaterial: (formData) => postRequest('/media/ai-clip/material/upload', formData),
  queryTemplate: (param) => postRequest('/media/ai-clip/template/query', param),
  templateDetail: (id) => getRequest(`/media/ai-clip/template/detail/${id}`),
  queryScript: (param) => postRequest('/media/ai-clip/script/query', param),
  saveScript: (param) => postRequest('/media/ai-clip/script/save', param),
  queryTask: (param) => postRequest('/media/ai-clip/task/query', param),
  createTask: (param) => postRequest('/media/ai-clip/task/create', param),
};
