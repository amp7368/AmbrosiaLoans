import { Client } from '@api/io-model';
import { EntityState, StoreConfig } from '@datorama/akita';

import { AppEntityStore } from '../base/AppEntityStore';

export type ClientState = EntityState<Client, string>;

@StoreConfig({ name: 'client', idKey: 'uuid' })
export class ClientStore extends AppEntityStore<ClientState> {}
export const clientStore = new ClientStore();
