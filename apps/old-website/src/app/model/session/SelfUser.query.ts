import { Optional } from '@appleptr16/utilities';
import { Observable } from 'rxjs';

import { QueryBase } from '../QueryBase';
import { SelfUser } from './SelfUser.model';
import { Session } from './Session.model';
import { selfUserStore } from './SelfUser.store';
import { Client } from '../user/Client.model';

export class SelfUserQuery extends QueryBase<SelfUser> {
    getSessionToken(): Optional<string> {
        return this.getValue()?.session?.sessionToken;
    }
    session: Observable<Optional<Session>> = this.selectKey('session');
    profile: Observable<Optional<Client>> = this.selectKey('profile');

    isLoggedIn: Observable<boolean> = this.map(
        this.session,
        (session: Optional<Session>) => {
            return !!session && new Date() < session.expiration;
        }
    );
}
export const selfUserQuery = new SelfUserQuery(selfUserStore);
